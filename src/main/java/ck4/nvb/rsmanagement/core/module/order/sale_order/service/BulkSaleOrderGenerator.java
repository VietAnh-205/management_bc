package ck4.nvb.rsmanagement.core.module.order.sale_order.service;

import ck4.nvb.rsmanagement.base.util.SnowflakeIdGeneratorHolder;
import ck4.nvb.rsmanagement.core.module.order.customer.domain.Customer;
import ck4.nvb.rsmanagement.core.module.order.customer.domain.CustomerRepository;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain.PaymentMethod;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain.PaymentMethodRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleAllocationRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleLineRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleOrderRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto.SaleLineDto;
import ck4.nvb.rsmanagement.core.module.order.voucher.domain.Voucher;
import ck4.nvb.rsmanagement.core.module.order.voucher.domain.VoucherRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchItem;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchItemRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStock;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStockRepository;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.Product;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.ProductRepository;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BulkSaleOrderGenerator {

  private static final Logger log = LoggerFactory.getLogger(BulkSaleOrderGenerator.class);

  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;
  private final PaymentMethodRepository paymentMethodRepository;
  private final VoucherRepository voucherRepository;
  private final BatchStockRepository batchStockRepository;
  private final BatchItemRepository batchItemRepository;
  private final SaleLineRepository saleLineRepository;
  private final SaleOrderRepository saleOrderRepository;
  private final SaleAllocationRepository saleAllocationRepository;
  private final DataSource dataSource;

  @Value("${order.generator.safety-stock-pct}")
  private double safetyStockPct;

  @Value("${order.generator.daily-total}")
  private int dailyTotalOrders;

  @Value("${order.generator.realtime-hour-weights}")
  private String realtimeHourWeightsProp;

  @Value("${order.generator.enable-allocation}")
  private boolean enableAllocation;

  private final ThreadLocalRandom rnd = ThreadLocalRandom.current();

  public BulkSaleOrderGenerator(
      CustomerRepository customerRepository,
      ProductRepository productRepository,
      PaymentMethodRepository paymentMethodRepository,
      VoucherRepository voucherRepository,
      BatchStockRepository batchStockRepository,
      BatchItemRepository batchItemRepository,
      SaleLineRepository saleLineRepository,
      SaleOrderRepository saleOrderRepository,
      SaleAllocationRepository saleAllocationRepository,
      DataSource dataSource) {
    this.customerRepository = customerRepository;
    this.productRepository = productRepository;
    this.paymentMethodRepository = paymentMethodRepository;
    this.voucherRepository = voucherRepository;
    this.batchStockRepository = batchStockRepository;
    this.batchItemRepository = batchItemRepository;
    this.saleLineRepository = saleLineRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.saleAllocationRepository = saleAllocationRepository;
    this.dataSource = dataSource;
  }

  private static class OrderDto {
    int tempIndex; // local mapping index
    String idString;
    Long storeId;
    Long customerId;
    Long paymentId;
    Long voucherId;
    LocalDateTime createdTime;
    int finalPrice;
    String externalRef;
  }

  private static class LineDto {
    int tempLineIndex;
    int tempOrderIndex;
    Long id;
    Long productId;
    int qtyOrdered;
    int unitPrice;
    LocalDateTime createdTime;
  }

  private static class AllocDto {
    int tempLineIndex;
    Long id;
    Long batchItemId;
    Long batchStockId;
    int soldQty;
    int unitCostSnap;
    LocalDateTime createdTime;
  }

  public void generateRange(LocalDate start, LocalDate end, int batchSize, int parallelism)
      throws Exception {
    log.info(
        "Bulk generate range {} -> {}, batchSize={}, threads={}",
        start,
        end,
        batchSize,
        parallelism);

    List<Customer> customers = customerRepository.findAll();
    List<Product> allProducts = productRepository.findAll();
    List<PaymentMethod> payments = paymentMethodRepository.findAll();
    List<Voucher> vouchers = voucherRepository.findAll();

    List<BatchStock> activeBatchStocks =
        batchStockRepository.findAll().stream()
            .filter(
                bs ->
                    bs != null
                        && bs.getStatus() != null
                        && "ACTIVE".equalsIgnoreCase(bs.getStatus()))
            .collect(Collectors.toList());
    if (activeBatchStocks.isEmpty()) {
      log.warn("No active batch stocks -> nothing to generate.");
      return;
    }
    List<Long> stores =
        activeBatchStocks.stream()
            .map(BatchStock::getStoreId)
            .distinct()
            .collect(Collectors.toList());
    if (stores.isEmpty()) {
      log.warn("No stores found -> nothing.");
      return;
    }

    // Preload batchItems per store
    Map<Long, List<BatchItem>> batchItemsPerStore = new HashMap<>();
    List<BatchItem> allBatchItems = batchItemRepository.findAll();
    Map<Long, List<BatchStock>> batchStocksByStore =
        activeBatchStocks.stream().collect(Collectors.groupingBy(BatchStock::getStoreId));
    for (Long storeId : stores) {
      Set<Long> batchIds =
          batchStocksByStore.getOrDefault(storeId, Collections.emptyList()).stream()
              .map(BatchStock::getBatchId)
              .collect(Collectors.toSet());
      List<BatchItem> list =
          allBatchItems.stream()
              .filter(bi -> bi != null && batchIds.contains(bi.getBatchId()))
              .sorted(
                  Comparator.comparing(
                      BatchItem::getManufactureDate,
                      Comparator.nullsLast(Comparator.naturalOrder())))
              .collect(Collectors.toList());
      batchItemsPerStore.put(storeId, list);
    }

    // Precompute available products per store
    Map<Long, List<Product>> productsPerStore = new HashMap<>();
    for (Long storeId : stores) {
      Set<Long> batchIds =
          batchStocksByStore.getOrDefault(storeId, Collections.emptyList()).stream()
              .map(BatchStock::getBatchId)
              .collect(Collectors.toSet());
      Set<Long> prodIds =
          allBatchItems.stream()
              .filter(bi -> bi != null && batchIds.contains(bi.getBatchId()))
              .map(BatchItem::getProductId)
              .collect(Collectors.toSet());
      List<Product> list =
          allProducts.stream()
              .filter(p -> p != null && prodIds.contains(p.getId()))
              .collect(Collectors.toList());
      productsPerStore.put(storeId, list);
    }

    ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, parallelism));
    List<Future<?>> futures = new ArrayList<>();

    // Distribute dailyTotalOrders across stores (global total)
    int totalOrders = Math.max(1, this.dailyTotalOrders);
    int perStoreBase = totalOrders / stores.size();
    int remainder = totalOrders % stores.size();

    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
      LocalDate currentDate = date;
      for (int si = 0; si < stores.size(); si++) {
        Long storeId = stores.get(si);
        int ordersForStore = perStoreBase + (si < remainder ? 1 : 0);
        List<Product> storeProducts =
            productsPerStore.getOrDefault(storeId, Collections.emptyList());
        List<BatchItem> storeBatchItems =
            batchItemsPerStore.getOrDefault(storeId, Collections.emptyList());
        if (storeProducts.isEmpty()) continue;
        Runnable task =
            () -> {
              try {
                generateForStoreOnDate(
                    currentDate,
                    storeId,
                    ordersForStore,
                    customers,
                    payments,
                    vouchers,
                    storeProducts,
                    storeBatchItems,
                    batchSize);
              } catch (Exception e) {
                log.error("Error generating for store " + storeId + " on " + currentDate, e);
              }
            };
        futures.add(executor.submit(task));
      }
    }

    for (Future<?> f : futures) {
      try {
        f.get();
      } catch (ExecutionException ee) {
        log.error("Execution failure", ee.getCause());
      }
    }
    executor.shutdown();
    log.info("Bulk generation finished.");
  }

  // --------------------
  // Per-store per-date generator
  // --------------------
  private void generateForStoreOnDate(
      LocalDate date,
      Long storeId,
      int ordersForStore,
      List<Customer> customers,
      List<PaymentMethod> payments,
      List<Voucher> vouchers,
      List<Product> availableProducts,
      List<BatchItem> storeBatchItems,
      int batchSize)
      throws Exception {

    double[] hourWeights = defaultHourWeights();
    double sumWeights = Arrays.stream(hourWeights).sum();

    Map<Integer, Integer> ordersPerHour = new HashMap<>();
    for (int h = 0; h < 24; h++) {
      double expected = ordersForStore * (hourWeights[h] / sumWeights);
      int sampled = samplePoisson(Math.max(0.0, expected));
      ordersPerHour.put(h, Math.max(0, sampled));
    }

    List<OrderDto> ordersBatch = new ArrayList<>(batchSize);
    List<LineDto> linesBatch = new ArrayList<>(batchSize * 3);
    List<AllocDto> allocBatch = new ArrayList<>(batchSize * 3);

    int tempOrderCounter = 0;
    int tempLineCounter = 0;

    // local remaining per batchItem for in-memory allocation simulation
    Map<Long, Integer> batchItemRemaining = new HashMap<>();
    for (BatchItem bi : storeBatchItems) {
      int original = bi.getOriginalQty() == null ? 0 : bi.getOriginalQty();
      batchItemRemaining.put(bi.getId(), original);
    }

    for (int hour = 0; hour < 24; hour++) {
      int nOrdersThisHour = ordersPerHour.getOrDefault(hour, 0);
      for (int i = 0; i < nOrdersThisHour; i++) {
        int minute = rnd.nextInt(0, 60);
        int second = rnd.nextInt(0, 60);
        LocalDateTime orderTime = LocalDateTime.of(date, LocalTime.of(hour, minute, second));

        // choose customer/payment/voucher (simplified)
        Customer chosenCustomer = null;
        if (!customers.isEmpty() && rnd.nextDouble() < 0.7)
          chosenCustomer = customers.get(rnd.nextInt(customers.size()));
        PaymentMethod chosenPayment =
            payments.isEmpty() ? null : payments.get(rnd.nextInt(payments.size()));
        Voucher chosenVoucher = null;
        if (!vouchers.isEmpty() && rnd.nextDouble() < 0.12)
          chosenVoucher = vouchers.get(rnd.nextInt(vouchers.size()));

        int numLines = sampleNumLinesAdvanced("regular", storeId, orderTime);

        Map<Long, Double> rawScoreMap = computeRawScoresMap(availableProducts, storeId, orderTime);

        List<Product> prodList = availableProducts;
        double[] scores = new double[prodList.size()];
        for (int pi = 0; pi < prodList.size(); pi++)
          scores[pi] = rawScoreMap.getOrDefault(prodList.get(pi).getId(), 0.0);
        double temperature = adaptTemperatureByHour(orderTime.getHour());
        double[] probs = softmax(scores, temperature);

        boolean[] used = new boolean[prodList.size()];
        List<SaleLineDto> saleLineDtos = new ArrayList<>();
        List<Product> chosenProducts = new ArrayList<>();
        int attempts = 0;
        while (saleLineDtos.size() < numLines && attempts < numLines * 12) {
          attempts++;
          Product chosenProduct = weightedChoiceProductWithMask(prodList, probs, used);
          if (chosenProduct == null) break;
          long avail = computeAvailableQtyForProductInStore(chosenProduct.getId(), storeId);
          if (avail <= 0) {
            for (int k = 0; k < prodList.size(); k++)
              if (prodList.get(k).getId().equals(chosenProduct.getId())) used[k] = true;
            continue;
          }
          int qty = sampleQtyByCategoryImproved(chosenProduct, avail, "regular");
          if (qty <= 0) {
            for (int k = 0; k < prodList.size(); k++)
              if (prodList.get(k).getId().equals(chosenProduct.getId())) used[k] = true;
            continue;
          }
          boolean dup =
              saleLineDtos.stream().anyMatch(l -> l.getProductId().equals(chosenProduct.getId()));
          if (dup) {
            for (int k = 0; k < prodList.size(); k++)
              if (prodList.get(k).getId().equals(chosenProduct.getId())) used[k] = true;
            continue;
          }
          SaleLineDto l = new SaleLineDto();
          l.setProductId(chosenProduct.getId());
          l.setQtyOrdered(qty);
          l.setUnitPrice(getUnitPriceSafe(chosenProduct));
          saleLineDtos.add(l);
          chosenProducts.add(chosenProduct);

          if (saleLineDtos.size() < numLines
              && rnd.nextDouble() < companionProbabilityBySegment("regular")) {
            Optional<Product> companion =
                findCompanionByCategoryAdvanced(
                    prodList, chosenProduct, used, storeId, rawScoreMap);
            if (companion.isPresent()) {
              Product cpp = companion.get();
              long av2 = computeAvailableQtyForProductInStore(cpp.getId(), storeId);
              if (av2 > 0) {
                int q2 = sampleQtyByCategoryImproved(cpp, av2, "regular");
                SaleLineDto l2 = new SaleLineDto();
                l2.setProductId(cpp.getId());
                l2.setQtyOrdered(q2);
                l2.setUnitPrice(getUnitPriceSafe(cpp));
                saleLineDtos.add(l2);
                chosenProducts.add(cpp);
                for (int k = 0; k < prodList.size(); k++)
                  if (prodList.get(k).getId().equals(cpp.getId())) used[k] = true;
              }
            }
          }
        }

        if (saleLineDtos.isEmpty()) continue;

        int preTotal = 0;
        for (SaleLineDto dto : saleLineDtos) preTotal += dto.getUnitPrice() * dto.getQtyOrdered();
        if (shouldAbandonCart("regular", preTotal)) continue;

        OrderDto od = new OrderDto();
        od.tempIndex = tempOrderCounter++;
        od.idString = UUID.randomUUID().toString();
        od.storeId = storeId;
        od.customerId = chosenCustomer == null ? null : chosenCustomer.getId();
        od.paymentId = chosenPayment == null ? null : chosenPayment.getId();
        od.voucherId = chosenVoucher == null ? null : chosenVoucher.getId();
        od.createdTime = orderTime;
        od.finalPrice = preTotal;
        od.externalRef = UUID.randomUUID().toString();
        ordersBatch.add(od);

        for (SaleLineDto dto : saleLineDtos) {
          LineDto ld = new LineDto();
          ld.tempLineIndex = tempLineCounter;
          ld.tempOrderIndex = od.tempIndex;
          ld.id = SnowflakeIdGeneratorHolder.getInstance().nextId();
          ld.productId = dto.getProductId();
          ld.qtyOrdered = dto.getQtyOrdered();
          ld.unitPrice = dto.getUnitPrice();
          ld.createdTime = orderTime;
          linesBatch.add(ld);

          // ---- allocation simulation ----
          if (enableAllocation) {
            int remaining = ld.qtyOrdered;
            for (BatchItem bi : storeBatchItems) {
              if (remaining <= 0) break;
              int orig = bi.getOriginalQty() == null ? 0 : bi.getOriginalQty();
              int soldAlready = 0;
              int rem =
                  batchItemRemaining.getOrDefault(bi.getId(), Math.max(0, orig - soldAlready));
              int safeKeep = (int) Math.ceil(orig * safetyStockPct);
              rem = Math.max(0, rem - safeKeep);
              if (rem <= 0) continue;
              int allocate = Math.min(remaining, rem);
              AllocDto ad = new AllocDto();
              ad.tempLineIndex = ld.tempLineIndex;
              ad.id = SnowflakeIdGeneratorHolder.getInstance().nextId();
              ad.batchItemId = bi.getId();
              Optional<BatchStock> bsOpt =
                  batchStockRepository.findFirstByBatchIdAndStoreId(bi.getBatchId(), storeId);
              ad.batchStockId = bsOpt.map(BatchStock::getId).orElse(null);
              ad.soldQty = allocate;
              ad.unitCostSnap = bi.getImportPrice() == null ? 0 : bi.getImportPrice();
              ad.createdTime = orderTime;
              allocBatch.add(ad);
              batchItemRemaining.put(bi.getId(), rem - allocate);
              remaining -= allocate;
            }
          }

          tempLineCounter++;
        }

        // apply voucher naive
        if (od.voucherId != null) {
          Voucher v =
              vouchers.stream()
                  .filter(x -> x.getId().equals(od.voucherId))
                  .findFirst()
                  .orElse(null);
          if (v != null) {
            if (v.getDiscountPer() != null && v.getDiscountPer() > 0) {
              od.finalPrice = (int) Math.round(od.finalPrice * (1.0 - v.getDiscountPer() / 100.0));
            } else if (v.getDiscountVal() != null && v.getDiscountVal() > 0) {
              od.finalPrice = Math.max(0, od.finalPrice - v.getDiscountVal());
            }
          }
        }

        // flush if needed
        if (ordersBatch.size() >= batchSize) {
          flushBatches(ordersBatch, linesBatch, allocBatch);
          ordersBatch.clear();
          linesBatch.clear();
          allocBatch.clear();
        }
      }
    }

    // flush remainder
    if (!ordersBatch.isEmpty()) {
      flushBatches(ordersBatch, linesBatch, allocBatch);
      ordersBatch.clear();
      linesBatch.clear();
      allocBatch.clear();
    }

    log.info("Finished generating for store {} on {}", storeId, date);
  }

  // --------------------
  // Batch flush pipeline
  // --------------------
  private void flushBatches(
      List<OrderDto> ordersBatch, List<LineDto> linesBatch, List<AllocDto> allocBatch) {
    if (ordersBatch == null || ordersBatch.isEmpty()) return;

    try (Connection conn = dataSource.getConnection()) {
      conn.setAutoCommit(false);

      // detect whether DB auto-generates id for each table
      boolean dbGenOrderId = false, dbGenLineId = false, dbGenAllocId = false;
      try (PreparedStatement metaPs =
          conn.prepareStatement(
              "SELECT TABLE_NAME, COLUMN_DEFAULT, EXTRA FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND COLUMN_NAME='id' AND TABLE_NAME IN ('sale_order','sale_line','sale_allocation')")) {
        try (ResultSet rs = metaPs.executeQuery()) {
          while (rs.next()) {
            String table = rs.getString("TABLE_NAME");
            String colDefault = rs.getString("COLUMN_DEFAULT");
            String extra = rs.getString("EXTRA");
            boolean generated =
                (colDefault != null && !colDefault.trim().isEmpty())
                    || (extra != null && extra.toLowerCase().contains("auto_increment"))
                    || (extra != null && extra.toLowerCase().contains("generated"));
            if ("sale_order".equalsIgnoreCase(table)) dbGenOrderId = generated;
            if ("sale_line".equalsIgnoreCase(table)) dbGenLineId = generated;
            if ("sale_allocation".equalsIgnoreCase(table)) dbGenAllocId = generated;
          }
        }
      } catch (SQLException e) {
        log.warn(
            "Could not read INFORMATION_SCHEMA for id metadata, default to application-supplied ids",
            e);
        dbGenOrderId = false;
        dbGenLineId = false;
        dbGenAllocId = false;
      }

      try {
        // ---------- INSERT ORDERS ----------
        if (!dbGenOrderId) {
          String sqlOrder =
              "INSERT INTO sale_order (id, store_id, customer_id, payment_id, created_by, created_at, updated_by, updated_at, voucher_id, final_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
          try (PreparedStatement ps = conn.prepareStatement(sqlOrder)) {
            for (OrderDto o : ordersBatch) {
              ps.setString(1, o.idString);
              ps.setLong(2, o.storeId);
              if (o.customerId == null) ps.setNull(3, Types.BIGINT);
              else ps.setLong(3, o.customerId);
              if (o.paymentId == null) ps.setNull(4, Types.BIGINT);
              else ps.setLong(4, o.paymentId);
              ps.setLong(5, 0L);
              ps.setTimestamp(6, Timestamp.valueOf(o.createdTime));
              ps.setLong(7, 0L);
              ps.setTimestamp(8, Timestamp.valueOf(o.createdTime));
              if (o.voucherId == null) ps.setNull(9, Types.BIGINT);
              else ps.setLong(9, o.voucherId);
              ps.setInt(10, o.finalPrice);
              ps.addBatch();
            }
            ps.executeBatch();
          }
        } else {
          String sqlOrder =
              "INSERT INTO sale_order (store_id, customer_id, payment_id, created_by, created_at, updated_by, updated_at, voucher_id, final_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
          try (PreparedStatement ps =
              conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
            for (OrderDto o : ordersBatch) {
              ps.setLong(1, o.storeId);
              if (o.customerId == null) ps.setNull(2, Types.BIGINT);
              else ps.setLong(2, o.customerId);
              if (o.paymentId == null) ps.setNull(3, Types.BIGINT);
              else ps.setLong(3, o.paymentId);
              ps.setLong(4, 0L);
              ps.setTimestamp(5, Timestamp.valueOf(o.createdTime));
              ps.setLong(6, 0L);
              ps.setTimestamp(7, Timestamp.valueOf(o.createdTime));
              if (o.voucherId == null) ps.setNull(8, Types.BIGINT);
              else ps.setLong(8, o.voucherId);
              ps.setInt(9, o.finalPrice);
              ps.addBatch();
            }
            ps.executeBatch();
            List<String> generatedIds = new ArrayList<>();
            try (ResultSet rs = ps.getGeneratedKeys()) {
              while (rs.next()) generatedIds.add(rs.getString(1));
            }
            if (generatedIds.size() == ordersBatch.size()) {
              for (int i = 0; i < ordersBatch.size(); i++)
                ordersBatch.get(i).idString = generatedIds.get(i);
            } else {
              throw new SQLException(
                  "Driver didn't return generated keys for sale_order inserts (count mismatch).");
            }
          }
        }

        // map tempOrderIndex -> idString
        Map<Integer, String> tempToOrderIdString = new HashMap<>();
        for (OrderDto o : ordersBatch) tempToOrderIdString.put(o.tempIndex, o.idString);

        // ---------- INSERT LINES ----------
        String sqlLineWithId =
            "INSERT INTO sale_line (id, sale_order_id, product_id, qty_ordered, unit_price, created_at, updated_at, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlLineNoId =
            "INSERT INTO sale_line (sale_order_id, product_id, qty_ordered, unit_price, created_at, updated_at, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        List<LineDto> actualInsertedLinesOrder = new ArrayList<>();
        Map<Integer, Long> tempLineIndexToRealId = new HashMap<>();

        try (PreparedStatement ps2 =
            conn.prepareStatement(
                dbGenLineId ? sqlLineNoId : sqlLineWithId, Statement.RETURN_GENERATED_KEYS)) {
          for (LineDto l : linesBatch) {
            String mappedOrderId = tempToOrderIdString.get(l.tempOrderIndex);
            if (mappedOrderId == null) {
              log.warn(
                  "Skipping line tempLineIndex={} because mapped order not found (tempOrderIndex={})",
                  l.tempLineIndex,
                  l.tempOrderIndex);
              continue;
            }
            int p = 1;
            if (!dbGenLineId) {
              ps2.setLong(p++, l.id);
            }
            ps2.setString(p++, mappedOrderId);
            ps2.setLong(p++, l.productId);
            ps2.setInt(p++, l.qtyOrdered);
            ps2.setInt(p++, l.unitPrice);
            ps2.setTimestamp(p++, Timestamp.valueOf(l.createdTime));
            ps2.setTimestamp(p++, Timestamp.valueOf(l.createdTime));
            ps2.setLong(p++, 0L);
            ps2.setLong(p++, 0L);
            ps2.addBatch();
            actualInsertedLinesOrder.add(l);
          }
          ps2.executeBatch();

          if (dbGenLineId) {
            List<Long> generatedLineIds = new ArrayList<>();
            try (ResultSet rs2 = ps2.getGeneratedKeys()) {
              while (rs2.next()) generatedLineIds.add(rs2.getLong(1));
            }
            if (generatedLineIds.size() == actualInsertedLinesOrder.size()) {
              for (int i = 0; i < actualInsertedLinesOrder.size(); i++) {
                tempLineIndexToRealId.put(
                    actualInsertedLinesOrder.get(i).tempLineIndex, generatedLineIds.get(i));
              }
            } else {
              throw new SQLException(
                  "Driver didn't return generated keys for sale_line inserts (count mismatch).");
            }
          } else {
            for (LineDto l : actualInsertedLinesOrder) {
              tempLineIndexToRealId.put(l.tempLineIndex, l.id);
            }
          }
        }

        // ---------- INSERT ALLOCATIONS ----------
        if (enableAllocation && !allocBatch.isEmpty()) {
          String sqlAllocWithId =
              "INSERT INTO sale_allocation (id, sale_line_id, batch_stock_id, sold_qty, unit_cost_snap, created_at, created_by, updated_at, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
          String sqlAllocNoId =
              "INSERT INTO sale_allocation (sale_line_id, batch_stock_id, sold_qty, unit_cost_snap, created_at, created_by, updated_at, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

          List<AllocDto> actualInsertedAllocs = new ArrayList<>();
          try (PreparedStatement ps3 =
              dbGenAllocId
                  ? conn.prepareStatement(sqlAllocNoId, Statement.RETURN_GENERATED_KEYS)
                  : conn.prepareStatement(sqlAllocWithId)) {
            for (AllocDto ad : allocBatch) {
              Long realLineId = tempLineIndexToRealId.get(ad.tempLineIndex);
              if (realLineId == null) {
                log.debug(
                    "Allocation skipped because realLineId not found for tempLineIndex {}",
                    ad.tempLineIndex);
                continue;
              }
              int p = 1;
              if (!dbGenAllocId) {
                ps3.setLong(p++, ad.id);
              }
              ps3.setLong(p++, realLineId);
              if (ad.batchStockId == null) ps3.setNull(p++, Types.BIGINT);
              else ps3.setLong(p++, ad.batchStockId);
              ps3.setInt(p++, ad.soldQty);
              ps3.setInt(p++, ad.unitCostSnap);
              ps3.setTimestamp(p++, Timestamp.valueOf(ad.createdTime));
              ps3.setLong(p++, 0L);
              ps3.setTimestamp(p++, Timestamp.valueOf(ad.createdTime));
              ps3.setLong(p++, 0L);
              ps3.addBatch();
              actualInsertedAllocs.add(ad);
            }
            ps3.executeBatch();

            if (dbGenAllocId) {
              List<Long> generatedAllocIds = new ArrayList<>();
              try (ResultSet rs3 = ps3.getGeneratedKeys()) {
                while (rs3.next()) generatedAllocIds.add(rs3.getLong(1));
              }
              if (generatedAllocIds.size() == actualInsertedAllocs.size()) {
                for (int i = 0; i < actualInsertedAllocs.size(); i++) {
                  actualInsertedAllocs.get(i).id = generatedAllocIds.get(i);
                }
              } else {
                // nếu bạn cần map id trả về cho business logic, xử lý mismatch ở đây
                log.warn(
                    "Driver didn't return generated keys for sale_allocation inserts (count mismatch).");
              }
            }
          }
        }

        conn.commit();
        log.debug(
            "Flushed batch: orders={}, lines={}, allocs={}",
            ordersBatch.size(),
            linesBatch.size(),
            allocBatch.size());
      } catch (SQLException ex) {
        try {
          conn.rollback();
        } catch (SQLException re) {
          log.error("Failed rollback after exception", re);
        }
        log.error(
            "Batch flush failed, rolled back. OrdersBatchSize={}, LinesBatchSize={}, AllocsSize={}",
            ordersBatch.size(),
            linesBatch.size(),
            allocBatch.size(),
            ex);
        return;
      }
    } catch (SQLException e) {
      log.error("Failed to obtain DB connection for flush", e);
    }
  }

  // --------------------
  // helpers (adapted from original generator)
  // --------------------
  private double[] defaultHourWeights() {
    String prop = realtimeHourWeightsProp == null ? "" : realtimeHourWeightsProp.trim();
    String[] parts = prop.isEmpty() ? new String[0] : prop.split("\\s*,\\s*");
    int[] weights = new int[24];
    boolean parsed = false;
    if (parts.length == 24) {
      parsed = true;
      for (int i = 0; i < 24; i++) {
        try {
          weights[i] = Math.max(0, Integer.parseInt(parts[i]));
        } catch (Exception e) {
          weights[i] = 0;
        }
      }
    }
    if (!parsed) {
      int[] def =
          new int[] {1, 1, 2, 3, 4, 3, 2, 3, 4, 3, 2, 1, 2, 3, 4, 3, 2, 1, 1, 3, 4, 3, 1, 1};
      for (int i = 0; i < 24; i++) weights[i] = def[i];
    }
    double[] w = new double[24];
    for (int i = 0; i < 24; i++) w[i] = weights[i];
    return w;
  }

  private int samplePoisson(double lambda) {
    double L = Math.exp(-lambda);
    int k = 0;
    double p = 1.0;
    while (p > L) {
      k++;
      p *= rnd.nextDouble();
      if (k > 100) break;
    }
    return Math.max(0, k - 1);
  }

  private double adaptTemperatureByHour(int hour) {
    if (hour >= 11 && hour <= 13) return 0.8;
    if (hour >= 17 && hour <= 20) return 0.9;
    return 1.1;
  }

  private Map<Long, Double> computeRawScoresMap(
      List<Product> products, Long storeId, LocalDateTime refTime) {
    Map<Long, Double> map = new HashMap<>();
    try {
      LocalDateTime to = refTime;
      LocalDateTime from = to.minusDays(60);
      int numberOfProducts = Math.max(50, products.size());
      List<Map<String, Object>> topSold =
          saleLineRepository.findMostSoldProductsOfIntervalWithQtyOfAStore(
              from, to, numberOfProducts, storeId);
      Map<Long, Long> qtyMap = new HashMap<>();
      if (topSold != null) {
        for (Map<String, Object> r : topSold) {
          try {
            Object idObj = r.get("id");
            Object qtyObj = r.get("totalQuantitySold");
            Long id =
                idObj == null
                    ? null
                    : (idObj instanceof Number
                        ? ((Number) idObj).longValue()
                        : Long.parseLong(idObj.toString()));
            Long qty =
                qtyObj == null
                    ? 0L
                    : (qtyObj instanceof Number
                        ? ((Number) qtyObj).longValue()
                        : Long.parseLong(qtyObj.toString()));
            if (id != null) qtyMap.put(id, qty);
          } catch (Exception e) {
          }
        }
      }
      int hour = refTime.getHour();
      int month = refTime.getMonthValue();
      DayOfWeek dow = refTime.getDayOfWeek();
      Map<String, double[]> seasonalMultipliers = new HashMap<>();
      seasonalMultipliers.put(
          "drink", new double[] {1.0, 1.0, 1.0, 1.0, 1.05, 1.1, 1.2, 1.25, 1.15, 1.05, 1.0, 1.0});
      seasonalMultipliers.put(
          "fruit", new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.3, 1.25, 1.1, 1.0, 1.0});
      seasonalMultipliers.put(
          "snack", new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.05, 1.05, 1.05, 1.05, 1.0, 1.0, 1.0});
      seasonalMultipliers.put(
          "coffee", new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.05, 1.1});

      Map<String, double[]> hourBoosts = new HashMap<>();
      hourBoosts.put(
          "drink", createHourBoostArray(24, 1.0, new int[][] {{11, 13, 14}}, new double[] {1.4}));
      hourBoosts.put(
          "coffee", createHourBoostArray(24, 1.0, new int[][] {{6, 9}}, new double[] {1.5}));
      hourBoosts.put(
          "snack", createHourBoostArray(24, 1.0, new int[][] {{17, 20}}, new double[] {1.3}));
      Map<String, Double> dowBoosts = new HashMap<>();
      dowBoosts.put("weekend_snack", 1.15);
      Set<MonthDay> simpleHolidays = new HashSet<>();
      simpleHolidays.add(MonthDay.of(1, 1));
      simpleHolidays.add(MonthDay.of(12, 25));

      for (Product p : products) {
        if (p == null) continue;
        long historicalQty = qtyMap.getOrDefault(p.getId(), 0L);
        double base = 1.0 + Math.log(1.0 + historicalQty);
        int up = getUnitPriceSafe(p);
        double priceFactor = 1.0;
        if (up < 10000) priceFactor = 1.6;
        else if (up < 30000) priceFactor = 1.3;
        else if (up < 100000) priceFactor = 1.0;
        else priceFactor = 0.6;
        String text =
            (p.getName() == null ? "" : p.getName()).toLowerCase()
                + " "
                + (p.getDescription() == null ? "" : p.getDescription()).toLowerCase();
        double catFactor = 1.0;
        String key = "other";
        if (text.contains("nước")
            || text.contains("juice")
            || text.contains("cola")
            || text.contains("nước ngọt")
            || text.contains("water")) {
          catFactor = 1.6;
          key = "drink";
        } else if (text.contains("snack")
            || text.contains("bánh")
            || text.contains("chips")
            || text.contains("kẹo")) {
          catFactor = 1.4;
          key = "snack";
        } else if (text.contains("trái")
            || text.contains("hoa quả")
            || text.contains("cam")
            || text.contains("táo")) {
          catFactor = 1.0;
          key = "fruit";
        } else if (text.contains("cà phê") || text.contains("coffee")) {
          catFactor = 1.2;
          key = "coffee";
        }
        double hourBoost = 1.0;
        if (hourBoosts.containsKey(key)) hourBoost = hourBoosts.get(key)[hour];
        if ((dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) && key.equals("snack"))
          hourBoost *= dowBoosts.getOrDefault("weekend_snack", 1.0);
        double season = 1.0;
        if (seasonalMultipliers.containsKey(key)) {
          double[] arr = seasonalMultipliers.get(key);
          season = arr[Math.max(0, Math.min(11, month - 1))];
        }
        double weight = base * priceFactor * catFactor * hourBoost * season;
        map.put(p.getId(), Math.max(0.01, weight));
      }

      double sum = map.values().stream().mapToDouble(Double::doubleValue).sum();
      if (sum > 0) {
        for (Map.Entry<Long, Double> e : map.entrySet()) e.setValue(e.getValue() / sum);
      }
    } catch (Exception ex) {
      for (Product p : products) if (p != null) map.put(p.getId(), 1.0);
    }
    return map;
  }

  private double[] createHourBoostArray(
      int length, double defaultVal, int[][] ranges, double[] boosts) {
    double[] arr = new double[length];
    Arrays.fill(arr, defaultVal);
    for (int i = 0; i < ranges.length; i++) {
      int from = ranges[i][0];
      int to = ranges[i][1];
      double b = boosts[i];
      for (int h = from; h <= to && h < length; h++) arr[h] = b;
    }
    return arr;
  }

  private double[] softmax(double[] scores, double temperature) {
    double max = Double.NEGATIVE_INFINITY;
    for (double s : scores) if (s > max) max = s;
    double[] exps = new double[scores.length];
    double sum = 0.0;
    double temp = Math.max(1e-6, temperature);
    for (int i = 0; i < scores.length; i++) {
      double v = Math.exp((scores[i] - max) / temp);
      exps[i] = v;
      sum += v;
    }
    if (sum <= 0) {
      double[] fallback = new double[scores.length];
      Arrays.fill(fallback, 1.0 / scores.length);
      return fallback;
    }
    for (int i = 0; i < scores.length; i++) exps[i] /= sum;
    return exps;
  }

  private Product weightedChoiceProductWithMask(
      List<Product> products, double[] probs, boolean[] used) {
    if (products == null || products.isEmpty()) return null;
    double total = 0.0;
    for (int i = 0; i < products.size(); i++) if (!used[i]) total += probs[i];
    if (total <= 0) return null;
    double r = rnd.nextDouble() * total;
    double acc = 0.0;
    for (int i = 0; i < products.size(); i++) {
      if (used[i]) continue;
      acc += probs[i];
      if (r <= acc) return products.get(i);
    }
    for (int i = 0; i < products.size(); i++) if (!used[i]) return products.get(i);
    return null;
  }

  private int sampleNumLinesAdvanced(String segment, Long storeId, LocalDateTime refTime) {
    boolean holiday = false;
    try {
      MonthDay md = MonthDay.from(refTime.toLocalDate());
      Set<MonthDay> simpleHolidays = new HashSet<>();
      simpleHolidays.add(MonthDay.of(1, 1));
      simpleHolidays.add(MonthDay.of(12, 25));
      holiday = simpleHolidays.contains(md);
    } catch (Exception e) {
    }
    int hour = refTime.getHour();
    double baseLambda;
    if ("vip".equals(segment)) baseLambda = 3.5;
    else if ("regular".equals(segment)) baseLambda = 2.0;
    else if ("newcomer".equals(segment)) baseLambda = 1.5;
    else baseLambda = 1.2;
    if (hour >= 11 && hour <= 14) baseLambda *= 1.1;
    if (hour >= 17 && hour <= 21) baseLambda *= 1.25;
    if (holiday) baseLambda *= 1.6;
    int sampled = samplePoisson(Math.max(0.5, baseLambda));
    sampled = Math.max(1, Math.min(8, sampled));
    return sampled;
  }

  private int sampleQtyByCategoryImproved(Product p, long available, String segment) {
    if (p == null) return 1;
    int up = getUnitPriceSafe(p);
    String text =
        (p.getName() == null ? "" : p.getName()).toLowerCase()
            + " "
            + (p.getDescription() == null ? "" : p.getDescription()).toLowerCase();
    List<Integer> packSizes = new ArrayList<>();
    if (text.contains("6-pack") || text.contains("6 lon") || text.contains("6pack"))
      packSizes.add(6);
    if (text.contains("12-pack") || text.contains("12 lon") || text.contains("12pack"))
      packSizes.add(12);
    if (text.contains("lốc 6") || text.contains("lốc")) packSizes.add(6);
    if (text.contains("nước")
        || text.contains("cola")
        || text.contains("juice")
        || text.contains("water")) {
      int[] options = {1, 1, 2, 2, 2, 6};
      int pick = options[rnd.nextInt(options.length)];
      if (!packSizes.isEmpty() && rnd.nextDouble() < 0.12)
        pick = packSizes.get(rnd.nextInt(packSizes.size()));
      pick = Math.min((int) available, pick);
      if (segment.equals("vip"))
        pick = Math.min((int) Math.max(1, pick + rnd.nextInt(0, 2)), (int) available);
      return Math.max(1, pick);
    }
    if (text.contains("snack")
        || text.contains("bánh")
        || text.contains("chips")
        || text.contains("kẹo")) {
      int[] opts = {1, 1, 2, 2, 3};
      int pick = opts[rnd.nextInt(opts.length)];
      if (segment.equals("vip")) pick += rnd.nextInt(0, 2);
      pick = Math.min(pick, (int) available);
      return Math.max(1, pick);
    }
    if (up > 200_000) return 1;
    if (up < 10000) {
      int pick = samplePoisson(2.0);
      pick = Math.min((int) available, Math.max(1, pick));
      return pick;
    }
    return Math.max(1, Math.min((int) available, 1 + rnd.nextInt(0, 3)));
  }

  private long computeAvailableQtyForProductInStore(Long productId, Long storeId) {
    if (productId == null || storeId == null) return 0;
    Long total = batchItemRepository.getTotalAvailableQtyForProductInStore(productId, storeId);
    if (total == null) return 0L;
    return total;
  }

  private int getUnitPriceSafe(Product p) {
    if (p == null) return 0;
    try {
      Integer up = p.getUnitPrice();
      return up == null ? 0 : up;
    } catch (Throwable t) {
      return 0;
    }
  }

  private boolean shouldAbandonCart(String segment, int preTotal) {
    double base = 0.35;
    if ("vip".equals(segment)) base -= 0.15;
    if ("guest".equals(segment)) base += 0.15;
    if (preTotal > 500_000) base += 0.05;
    return rnd.nextDouble() < base;
  }

  private double companionProbabilityBySegment(String seg) {
    switch (seg) {
      case "vip":
        return 0.3;
      case "regular":
        return 0.2;
      case "newcomer":
        return 0.5;
      default:
        return 0.08;
    }
  }

  private Optional<Product> findCompanionByCategoryAdvanced(
      List<Product> products,
      Product chosen,
      boolean[] used,
      Long storeId,
      Map<Long, Double> rawScoreMap) {
    String chosenText =
        (chosen.getName() == null ? "" : chosen.getName()).toLowerCase()
            + " "
            + (chosen.getDescription() == null ? "" : chosen.getDescription()).toLowerCase();
    boolean preferDrink =
        chosenText.contains("snack") || chosenText.contains("bánh") || chosenText.contains("chips");
    List<Integer> candidateIndices = new ArrayList<>();
    for (int i = 0; i < products.size(); i++) {
      if (used[i]) continue;
      Product p = products.get(i);
      String t =
          (p.getName() == null ? "" : p.getName()).toLowerCase()
              + " "
              + (p.getDescription() == null ? "" : p.getDescription()).toLowerCase();
      if (preferDrink
          && (t.contains("nước")
              || t.contains("cola")
              || t.contains("juice")
              || t.contains("nước ngọt"))) candidateIndices.add(i);
    }
    if (candidateIndices.isEmpty()) {
      List<Integer> indices = new ArrayList<>();
      for (int i = 0; i < products.size(); i++)
        if (!used[i] && !products.get(i).getId().equals(chosen.getId())) indices.add(i);
      indices.sort(
          (a, b) ->
              Double.compare(
                  rawScoreMap.getOrDefault(products.get(b).getId(), 0.0),
                  rawScoreMap.getOrDefault(products.get(a).getId(), 0.0)));
      if (!indices.isEmpty()) candidateIndices.add(indices.get(0));
    }
    if (candidateIndices.isEmpty()) return Optional.empty();
    int chosenIdx = candidateIndices.get(rnd.nextInt(candidateIndices.size()));
    return Optional.of(products.get(chosenIdx));
  }
}
