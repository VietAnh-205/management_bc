package ck4.nvb.rsmanagement.core.module.order.sale_order.service;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.base.application.exception.DuplicateIdentifierException;
import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchCriteria;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.order.customer.domain.Customer;
import ck4.nvb.rsmanagement.core.module.order.customer.domain.CustomerRepository;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain.PaymentMethod;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain.PaymentMethodRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleOrder;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleOrderRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto.*;
import ck4.nvb.rsmanagement.core.module.order.salereturn.domain.SaleReturnRepository;
import ck4.nvb.rsmanagement.core.module.order.voucher.domain.Voucher;
import ck4.nvb.rsmanagement.core.module.order.voucher.domain.VoucherRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchItem;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchItemRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStock;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStockRepository;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.ProductRepository;
import ck4.nvb.rsmanagement.core.module.stores.store.service.dto.AllStoresRevenueResponse;
import ck4.nvb.rsmanagement.core.module.stores.store.service.dto.RevenuePoint;
import ck4.nvb.rsmanagement.core.module.stores.store.service.dto.RevenueSeriesResponse;
import ck4.nvb.rsmanagement.core.module.stores.store.service.dto.StoreRevenueSeries;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("orderService")
@Transactional
public class SaleOrderServiceImpl
    extends FullAuditedCrudServiceImpl<SaleOrderGetFullDto, SaleOrder, String, UserGetDto, Long>
    implements ISaleOrderService {

  protected SaleOrderServiceImpl(SaleOrderRepository repository) {
    super(repository, SaleOrder.class);
  }

  @Override
  public SaleOrderRepository getRepository() {
    return (SaleOrderRepository) super.getRepository();
  }

  @Autowired private CustomerRepository customerRepository;
  @Autowired private VoucherRepository voucherRepository;
  @Autowired private PaymentMethodRepository paymentMethodRepository;
  @Autowired private ISaleLineService saleLineService;
  @Autowired private ISaleAllocationService saleAllocationService;
  @Autowired private ProductRepository productRepository;
  @Autowired private BatchRepository batchRepository;
  @Autowired private BatchStockRepository batchStockRepository;
  @Autowired private BatchItemRepository batchItemRepository;
  @Autowired private SaleReturnRepository saleReturnRepository;

  @Override
  public SaleOrderGetFullDto mapToEntityDto(SaleOrder entity) {
    SaleOrderGetFullDto orderDto = new SaleOrderGetFullDto();
    orderDto.setId(entity.getId());

    if (entity.getCustomerId() != null) {
      Customer customer = customerRepository.findFirstByIdAndDeletedIsFalse(entity.getCustomerId());
      orderDto.setCustomerId(entity.getCustomerId());
      orderDto.setCustomerName(customer.getName());
    }

    List<SaleLineGetDto> lines =
            saleLineService.getAll(
                    List.of(new SearchCriteria("saleOrderId", SearchOperator.EQUALS, entity.getId())));
    orderDto.setSaleLines(lines);

    orderDto.setStoreId(entity.getStoreId());
    orderDto.setNote(entity.getNote());

    if (entity.getVoucherId() != null) {
      Voucher voucher = voucherRepository.findFirstByIdAndDeletedIsFalse(entity.getVoucherId());
      orderDto.setVoucherCode(voucher.getCode());
    }

    PaymentMethod paymentMethod = paymentMethodRepository.getReferenceById(entity.getPaymentId());
    orderDto.setPaymentMethodName(paymentMethod.getName());

    orderDto.setFinalPrice(entity.getFinalPrice());
    return orderDto;
  }

  @Override
  public SaleOrderGetFullDto create(CreateInput<SaleOrder> createDto, UserGetDto user)
      throws AppException {
    if (createDto instanceof SaleOrderCreateDto) {
      return create((SaleOrderCreateDto) createDto, user);
    }
    return super.create(createDto, user);
  }

  public SaleOrderGetFullDto create(SaleOrderCreateDto createDto, UserGetDto user)
      throws AppException {
    super.checkCreatePermission(createDto, user);

    // check inventory availability first
    validateInventoryAvailability(createDto, user);

    int finalPrice = 0;
    SaleOrder saleOrder = createDto.mapToEntity();
    saleOrder.setStoreId(user.getStoreId());
    saleOrder.setCreatorId(user.getId());
    saleOrder.setCreatedTime(LocalDateTime.now());
    saleOrder.setNew(true);
    saleOrder.setUpdaterID(user.getId());
    saleOrder.setUpdatedTime(saleOrder.getCreatedTime());
    if (saleOrder.getId() != null && exists(saleOrder.getId())) {
      getLogger().error("Duplicate id {}", saleOrder.getId());
      throw new DuplicateIdentifierException("Duplicate identifier " + saleOrder.getId());
    }
    for (SaleLineDto saleLineDto : createDto.getLines()) {
      finalPrice +=
          productRepository
                  .findFirstByIdAndDeletedIsFalse(saleLineDto.getProductId())
                  .getUnitPrice()
              * saleLineDto.getQtyOrdered();
    }
    if (createDto.getVoucherCode() != null) {
      Voucher voucher =
          voucherRepository.findFirstByCodeAndDeletedFalse(createDto.getVoucherCode());
      saleOrder.setVoucherId(voucher.getId());
      Integer discount = 0;
      if (voucher.getDiscountPer() > 0) {
        discount = (finalPrice * voucher.getDiscountPer()) / 100;
      } else if (voucher.getDiscountVal() != null) {
        discount = voucher.getDiscountVal();
      }
      finalPrice -= discount;
    }
    saleOrder.setFinalPrice(finalPrice);
    saleOrder = getRepository().save(saleOrder);
    getLogger()
        .info(
            "Created order id {} by user {}: {}",
            saleOrder.getId(),
            saleOrder.getCreatorId(),
            saleOrder);

    for (SaleLineDto saleLineDto : createDto.getLines()) {
      saleLineDto.setSaleOrderId(saleOrder.getId());
      SaleLineGetDto createdLine = saleLineService.create(saleLineDto, user);

      // allocate inventory or this line
      allocateInventoryForSaleLine(
          createdLine.getId(), saleLineDto.getProductId(), saleLineDto.getQtyOrdered(), user);
    }

    return mapToEntityDto(saleOrder);
  }

  /** validate if there's enough inventory available for all products in the order */
  private void validateInventoryAvailability(SaleOrderCreateDto createDto, UserGetDto user)
      throws AppException {
    for (SaleLineDto lineDto : createDto.getLines()) {
      Long availableQty = getTotalAvailableQuantity(lineDto.getProductId(), user);
      if (availableQty < lineDto.getQtyOrdered()) {
        throw new AppException(
            String.format(
                "Insufficient inventory for product ID %d. Required: %d, Available: %d",
                lineDto.getProductId(), lineDto.getQtyOrdered(), availableQty));
      }
    }
  }

  /** get total available quantity for a product in a specific store */
  private Long getTotalAvailableQuantity(Long productId, UserGetDto user) throws AppException {
    List<BatchItem> batchItems =
        batchItemRepository.findAvailableByProductAndStoreOrdered(productId, user.getStoreId());
    long totalAvailable = 0L;

    for (BatchItem batchItem : batchItems) {
      Integer original = batchItem.getOriginalQty() == null ? 0 : batchItem.getOriginalQty();
      Integer sold = saleAllocationService.getTotalSoldQuantityByBatchItem(batchItem.getId());
      int avail = Math.max(0, original - (sold == null ? 0 : sold));
      totalAvailable += avail;
    }

    return totalAvailable;
  }

  /** Get total sold quantity for a specific product from a specific batch_stock */
  private Integer getSoldQuantityForBatchItem(Long batchStockId, Long productId) {
    // This would need a custom repository method or query
    // For now, using a simple approach - you might want to optimize this with a custom query
    return saleAllocationService.getTotalSoldQuantityByBatchStockAndProduct(
        batchStockId, productId);
  }

  /** allocate inventory for a sale line using FIFO strategy */
  private void allocateInventoryForSaleLine(
      Long saleLineId, Long productId, Integer qtyNeeded, UserGetDto user) throws AppException {
    List<BatchItem> availableBatchItems =
        batchItemRepository.findAvailableByProductAndStoreOrdered(productId, user.getStoreId());

    int remaining = qtyNeeded;
    for (BatchItem bi : availableBatchItems) {
      if (remaining <= 0) break;

      int original = bi.getOriginalQty() == null ? 0 : bi.getOriginalQty();
      int sold =
          saleAllocationService.getTotalSoldQuantityByBatchItem(bi.getId()) == null
              ? 0
              : saleAllocationService.getTotalSoldQuantityByBatchItem(bi.getId());
      int avail = Math.max(0, original - sold);
      if (avail <= 0) continue;

      int allocateQty = Math.min(remaining, avail);

      // --- NEW: resolve batchStockId for this batchItem ---
      Long batchStockId = null;
      // try to find BatchStock that matches this batchItem's batchId and the storeId
      try {
        Optional<BatchStock> bsOpt =
            batchStockRepository.findAll().stream()
                .filter(
                    bs ->
                        bs != null
                            && bs.getBatchId() != null
                            && bs.getBatchId().equals(bi.getBatchId())
                            && bs.getStoreId() != null
                            && bs.getStoreId().equals(user.getStoreId()))
                .findFirst();
        if (bsOpt.isPresent()) {
          batchStockId = bsOpt.get().getId();
        } else {
          // try matching by batchId only (less strict)
          bsOpt =
              batchStockRepository.findAll().stream()
                  .filter(
                      bs ->
                          bs != null
                              && bs.getBatchId() != null
                              && bs.getBatchId().equals(bi.getBatchId()))
                  .findFirst();
          if (bsOpt.isPresent()) batchStockId = bsOpt.get().getId();
        }
      } catch (Exception ex) {
        // repository call failed unexpectedly
        throw new AppException(
            "Failed to resolve batchStock for batchItem " + bi.getId() + ": " + ex.getMessage());
      }

      if (batchStockId == null) {
        // fail fast: avoid inserting allocation with null batch_stock_id
        throw new AppException(
            "Cannot allocate inventory: batch_stock not found for batchItem "
                + bi.getId()
                + " (batchId="
                + bi.getBatchId()
                + ", storeId="
                + user.getStoreId()
                + "). Please ensure batch_stock exists.");
      }

      // Create allocation DTO with batchStockId included
      SaleAllocationDto alloc = new SaleAllocationDto();
      alloc.setSaleLineId(saleLineId);
      alloc.setBatchItemId(bi.getId());
      alloc.setBatchStockId(batchStockId); // <<< important: fill batchStockId
      alloc.setSoldQty(allocateQty);
      alloc.setUnitCostSnap(bi.getImportPrice());
      saleAllocationService.create(alloc, user);

      remaining -= allocateQty;
      getLogger()
          .info(
              "Allocated {} units from batch_item {} (batch_stock {}) for sale line {}",
              allocateQty,
              bi.getId(),
              batchStockId,
              saleLineId);
    }

    if (remaining > 0) {
      throw new AppException(
          String.format(
              "Unable to fully allocate inventory for product %d. Missing %d units",
              productId, remaining));
    }
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("customerId", List.of(SearchOperator.EQUALS));
    keys.put("customerName", List.of(SearchOperator.EQUALS, SearchOperator.CONTAINS));
    keys.put("storeId", List.of(SearchOperator.EQUALS));
    keys.put("voucherId", List.of(SearchOperator.EQUALS));
    keys.put(
            "finalPrice",
            List.of(
                    SearchOperator.EQUALS,
                    SearchOperator.LESS_THAN,
                    SearchOperator.GREATER_THAN,
                    SearchOperator.GREATER_THAN_OR_EQUAL,
                    SearchOperator.LESS_THAN_OR_EQUAL));
    keys.put(
            "createdTime",
            List.of(
                    SearchOperator.EQUALS,
                    SearchOperator.LESS_THAN,
                    SearchOperator.GREATER_THAN,
                    SearchOperator.GREATER_THAN_OR_EQUAL,
                    SearchOperator.LESS_THAN_OR_EQUAL));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("finalPrice");
    keys.add("createdTime");
    return keys;
  }

  // ---------- helpers to compute time ranges ----------
  private LocalDateTime startOfDay(LocalDate date) {
    return date.atStartOfDay();
  }

  private LocalDateTime startOfNextDay(LocalDate date) {
    return date.plusDays(1).atStartOfDay();
  }

  private LocalDateTime startOfWeek(LocalDate date) {
    // tuần bắt đầu từ thứ Hai; nếu bạn muốn CN bắt đầu thì dùng previousOrSame(SUNDAY)
    LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    return monday.atStartOfDay();
  }

  private LocalDateTime startOfNextWeek(LocalDate date) {
    return startOfWeek(date).plusDays(7);
  }

  private LocalDateTime startOfMonth(LocalDate date) {
    return date.withDayOfMonth(1).atStartOfDay();
  }

  private LocalDateTime startOfNextMonth(LocalDate date) {
    return date.withDayOfMonth(1).plusMonths(1).atStartOfDay();
  }

  // ---------- public methods to get revenue ----------

  // gross revenue for one store between from..to
  public Long getAStoreRevenueBetween(LocalDateTime from, LocalDateTime to, Long storeId)
          throws AppException {
    Long revenue = getRepository().sumTotalFinalPriceOfAStoreBetween(from, to, storeId);
    return revenue == null ? 0L : revenue;
  }

  // net revenue (orders - returns) for one store between from..to (if saleReturnRepository
  // available)
  public Long getAStoreNetRevenueBetween(LocalDateTime from, LocalDateTime to, Long storeId)
          throws AppException {
    Long orders = getRepository().sumTotalFinalPriceOfAStoreBetween(from, to, storeId);
    orders = orders == null ? 0L : orders;
    Long returns = 0L;
    try {
      if (saleReturnRepository != null) {
        returns = saleReturnRepository.sumTotalReturnAmountOfAStoreBetween(from, to, storeId);
        returns = returns == null ? 0L : returns;
      }
    } catch (Exception ex) {
      // nếu repo không có hoặc lỗi, mặc định returns = 0
      returns = 0L;
    }
    return orders - returns;
  }

  // convenience API: revenue for a store on a day
  public Long getAStoreRevenueForDay(LocalDate date, Long storeId) throws AppException {
    LocalDateTime from = startOfDay(date);
    LocalDateTime to = startOfNextDay(date);
    return getAStoreRevenueBetween(from, to, storeId);
  }

  public Long getAStoreNetRevenueForDay(LocalDate date, Long storeId) throws AppException {
    LocalDateTime from = startOfDay(date);
    LocalDateTime to = startOfNextDay(date);
    return getAStoreNetRevenueBetween(from, to, storeId);
  }

  // revenue for a store in the week containing date
  public Long getAStoreRevenueForWeek(LocalDate date, Long storeId) throws AppException {
    LocalDateTime from = startOfWeek(date);
    LocalDateTime to = startOfNextWeek(date);
    return getAStoreRevenueBetween(from, to, storeId);
  }

  public Long getAStoreNetRevenueForWeek(LocalDate date, Long storeId) throws AppException {
    LocalDateTime from = startOfWeek(date);
    LocalDateTime to = startOfNextWeek(date);
    return getAStoreNetRevenueBetween(from, to, storeId);
  }

  // revenue for a store in the month containing date
  public Long getAStoreRevenueForMonth(LocalDate date, Long storeId) throws AppException {
    LocalDateTime from = startOfMonth(date);
    LocalDateTime to = startOfNextMonth(date);
    return getAStoreRevenueBetween(from, to, storeId);
  }

  public Long getAStoreNetRevenueForMonth(LocalDate date, Long storeId) throws AppException {
    LocalDateTime from = startOfMonth(date);
    LocalDateTime to = startOfNextMonth(date);
    return getAStoreNetRevenueBetween(from, to, storeId);
  }

  // ---------- series method ----------
  public RevenueSeriesResponse getAStoreRevenueSeries(Long storeId, int days) throws AppException {
    if (days <= 0) days = 30;
    LocalDate today = LocalDate.now();
    LocalDate fromDate = today.minusDays(days - 1);
    LocalDateTime from = fromDate.atStartOfDay();
    LocalDateTime to = startOfNextDay(today);

    List<Object[]> rows = getRepository().sumDailyRevenueOfAStoreBetween(from, to, storeId);

    Map<LocalDate, Long> map = new HashMap<>();
    if (rows != null) {
      for (Object[] r : rows) {
        if (r == null || r.length < 2) continue;
        java.sql.Date sqlDate = (java.sql.Date) r[0];
        Long rev = r[1] == null ? 0L : ((Number) r[1]).longValue();
        map.put(sqlDate.toLocalDate(), rev);
      }
    }

    List<RevenuePoint> series = new ArrayList<>();
    for (int i = 0; i < days; i++) {
      LocalDate d = fromDate.plusDays(i);
      Long rev = map.getOrDefault(d, 0L);
      series.add(new RevenuePoint(d.toString(), rev));
    }

    Long dayTotal = 0L;
    Long weekTotal = 0L;
    Long monthTotal = 0L;
    try {
      dayTotal = getAStoreRevenueForDay(today, storeId);
    } catch (Exception e) {
      dayTotal = 0L;
    }
    try {
      weekTotal = getAStoreRevenueForWeek(today, storeId);
    } catch (Exception e) {
      weekTotal = 0L;
    }
    try {
      monthTotal = getAStoreRevenueForMonth(today, storeId);
    } catch (Exception e) {
      monthTotal = 0L;
    }

    return new RevenueSeriesResponse(series, dayTotal, weekTotal, monthTotal);
  }

  public AllStoresRevenueResponse getAllStoresRevenueSeries(int days) throws AppException {
    if (days <= 0) days = 30;
    LocalDate today = LocalDate.now();
    LocalDate fromDate = today.minusDays(days - 1);
    LocalDateTime from = fromDate.atStartOfDay();
    LocalDateTime to = startOfNextDay(today); // exclusive

    List<Object[]> rows = getRepository().sumDailyRevenueAllStoresBetween(from, to);

    // map storeId -> storeName
    Map<Long, String> storeNames = new HashMap<>();
    Map<Long, Map<LocalDate, Long>> tmp = new HashMap<>();

    if (rows != null) {
      for (Object[] r : rows) {
        if (r == null || r.length < 4) continue;
        // r[0] = store_id, r[1] = store_name, r[2] = day, r[3] = revenue
        Long storeId = r[0] == null ? null : ((Number) r[0]).longValue();
        String storeName = r[1] == null ? ("Store " + storeId) : r[1].toString();
        java.sql.Date sqlDate = (java.sql.Date) r[2];
        Long rev = r[3] == null ? 0L : ((Number) r[3]).longValue();

        if (storeId == null) continue;
        storeNames.put(storeId, storeName);
        tmp.computeIfAbsent(storeId, k -> new HashMap<>()).put(sqlDate.toLocalDate(), rev);
      }
    }

    List<StoreRevenueSeries> stores = new ArrayList<>();
    for (Map.Entry<Long, Map<LocalDate, Long>> e : tmp.entrySet()) {
      Long storeId = e.getKey();
      Map<LocalDate, Long> map = e.getValue();
      List<RevenuePoint> series = new ArrayList<>();
      for (int i = 0; i < days; i++) {
        LocalDate d = fromDate.plusDays(i);
        Long r = map.getOrDefault(d, 0L);
        series.add(new RevenuePoint(d.toString(), r));
      }
      stores.add(new StoreRevenueSeries(storeId, storeNames.get(storeId), series));
    }

    // Các tổng (toàn hệ thống) cho today / week / month
    Long dayTotal = 0L, weekTotal = 0L, monthTotal = 0L;
    try {
      dayTotal =
              getRepository().sumTotalFinalPriceBetween(startOfDay(today), startOfNextDay(today));
      dayTotal = dayTotal == null ? 0L : dayTotal;
    } catch (Exception ex) {
      dayTotal = 0L;
    }

    try {
      LocalDateTime weekFrom = startOfWeek(today);
      LocalDateTime weekTo = startOfNextWeek(today);
      weekTotal = getRepository().sumTotalFinalPriceBetween(weekFrom, weekTo);
      weekTotal = weekTotal == null ? 0L : weekTotal;
    } catch (Exception ex) {
      weekTotal = 0L;
    }

    try {
      LocalDateTime monthFrom = startOfMonth(today);
      LocalDateTime monthTo = startOfNextMonth(today);
      monthTotal = getRepository().sumTotalFinalPriceBetween(monthFrom, monthTo);
      monthTotal = monthTotal == null ? 0L : monthTotal;
    } catch (Exception ex) {
      monthTotal = 0L;
    }

    return new AllStoresRevenueResponse(
            stores, dayTotal, weekTotal, monthTotal, fromDate.toString(), today.toString());
  }
  /**
   * Trả về doanh thu theo tuần cho một hoặc nhiều cửa hàng trong khoảng start..end.
   * start/end là LocalDateTime (start inclusive, end exclusive).
   * Nếu storeIds == null hoặc empty -> trả về cho tất cả cửa hàng (dữ liệu có trong DB).
   */
  public List<StoreRevenueSeries> getWeeklyRevenueForStores(
          LocalDateTime start, LocalDateTime end, List<Long> storeIds) throws AppException {

    LocalDate startDate = start.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate endDate = end.toLocalDate();

    List<LocalDate> weeks = new ArrayList<>();
    LocalDate cur = startDate;
    while (!cur.isAfter(endDate)) {
      weeks.add(cur);
      cur = cur.plusWeeks(1);
    }

    List<Object[]> rows;
    if (storeIds == null || storeIds.isEmpty()) {
      rows = getRepository().sumWeeklyRevenueAllStoresBetween(start, end);
    } else {
      rows = getRepository().sumWeeklyRevenueForStoresBetween(start, end, storeIds);
    }

    Map<Long, String> storeNames = new HashMap<>();
    Map<Long, Map<LocalDate, Long>> tmp = new HashMap<>();

    if (rows != null) {
      for (Object[] r : rows) {
        if (r == null || r.length < 4) continue;
        // r[0] = store_id, r[1] = store_name, r[2] = week_start (java.sql.Date), r[3] = revenue
        Long storeId = r[0] == null ? null : ((Number) r[0]).longValue();
        String storeName = r[1] == null ? ("Store " + storeId) : r[1].toString();

        LocalDate weekStart;
        if (r[2] instanceof java.sql.Date) {
          weekStart = ((java.sql.Date) r[2]).toLocalDate();
        } else if (r[2] instanceof java.sql.Timestamp) {
          weekStart = ((java.sql.Timestamp) r[2]).toLocalDateTime().toLocalDate();
        } else {
          weekStart = LocalDate.parse(r[2].toString());
        }

        Long rev = r[3] == null ? 0L : ((Number) r[3]).longValue();

        if (storeId == null) continue;
        storeNames.put(storeId, storeName);
        tmp.computeIfAbsent(storeId, k -> new HashMap<>()).put(weekStart, rev);
      }
    }

    List<StoreRevenueSeries> result = new ArrayList<>();
    for (Map.Entry<Long, Map<LocalDate, Long>> e : tmp.entrySet()) {
      Long storeId = e.getKey();
      Map<LocalDate, Long> weekMap = e.getValue();
      List<RevenuePoint> series = new ArrayList<>();
      for (LocalDate w : weeks) {
        Long v = weekMap.getOrDefault(w, 0L);
        series.add(new RevenuePoint(w.toString(), v));
      }
      result.add(new StoreRevenueSeries(storeId, storeNames.get(storeId), series));
    }
    if (storeIds != null) {
      for (Long sid : storeIds) {
        if (!tmp.containsKey(sid)) {
          List<RevenuePoint> series = new ArrayList<>();
          for (LocalDate w : weeks) series.add(new RevenuePoint(w.toString(), 0L));
          String name = "Store " + sid;
          result.add(new StoreRevenueSeries(sid, name, series));
        }
      }
    }

    // sắp xếp result theo storeId cho ổn định
    result.sort(Comparator.comparing(StoreRevenueSeries::getStoreId));
    return result;
  }

}
