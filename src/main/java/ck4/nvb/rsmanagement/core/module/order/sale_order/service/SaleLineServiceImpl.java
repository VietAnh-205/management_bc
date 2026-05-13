package ck4.nvb.rsmanagement.core.module.order.sale_order.service;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleLine;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleLineRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto.SaleLineDto;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto.SaleLineGetDto;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto.StoreWeekTopProductsDto;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.Product;
import ck4.nvb.rsmanagement.core.module.stores.product.service.ProductServiceImpl;
import ck4.nvb.rsmanagement.core.module.stores.product.service.dto.ProductGetDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("saleLineService")
public class SaleLineServiceImpl
    extends FullAuditedCrudServiceImpl<SaleLineGetDto, SaleLine, Long, UserGetDto, Long>
    implements ISaleLineService {

  @Autowired ProductServiceImpl productService;

  protected SaleLineServiceImpl(SaleLineRepository repository) {
    super(repository, SaleLine.class);
  }

  @Override
  public SaleLineRepository getRepository() {
    return (SaleLineRepository) super.getRepository();
  }

  @Override
  public SaleLineGetDto mapToEntityDto(SaleLine entity) {
    SaleLineGetDto dto = new SaleLineGetDto();

    dto.setId(entity.getId());

    dto.setSaleOrderId(entity.getSaleOrderId());

    Product product = productService.getEntity(entity.getProductId());
    // snapshot
    dto.setUnitPrice(
            product.getUnitPrice());
    dto.setProductId(product.getId());
    dto.setProductName(product.getName());

    dto.setQtyOrdered(entity.getQtyOrdered());

    Long totalPrice = (long) entity.getQtyOrdered() * entity.getUnitPrice();
    dto.setTotalPrice(totalPrice);

    return dto;
  }

  @Override
  public SaleLineGetDto create(CreateInput<SaleLine> createDto, UserGetDto user)
      throws AppException {
    if (createDto instanceof SaleLineDto) {
      Product product = productService.getEntity(((SaleLineDto) createDto).getProductId());
      ((SaleLineDto) createDto).setUnitPrice(product.getUnitPrice());
    }
    return super.create(createDto, user);
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("saleOrderId", List.of(SearchOperator.EQUALS));
    keys.put("productId", List.of(SearchOperator.EQUALS));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("quantity");
    return keys;
  }

  @Override
  public List<ProductGetDto.WithSales> getMostSoldProductsLastDay(int days, int noProducts)
      throws AppException {
    LocalDateTime end = LocalDateTime.now();
    LocalDateTime start = end.minusDays(days);

    List<Map<String, Object>> results =
        getRepository().findMostSoldProductsOfIntervalWithQty(start, end, noProducts);

    return getWithSales(results);
  }

  private List<ProductGetDto.WithSales> getWithSales(List<Map<String, Object>> results) {
    return results.stream()
        .map(
            row ->
                new ProductGetDto.WithSales(
                    ((Number) row.get("id")).longValue(),
                    (String) row.get("sku"),
                    (String) row.get("name"),
                    (String) row.get("description"),
                    ((Number) row.get("unitPrice")).intValue(),
                    row.get("categoryId") != null
                        ? ((Number) row.get("categoryId")).longValue()
                        : null,
                    ((Number) row.get("totalQuantitySold")).longValue()))
        .toList();
  }

  @Override
  public List<ProductGetDto.WithSales> getMostSoldProductsLastDayOfAStore(
      int days, int noProducts, Long storeId) {
    LocalDateTime end = LocalDateTime.now();
    LocalDateTime start = end.minusDays(days);

    List<Map<String, Object>> results =
        getRepository()
            .findMostSoldProductsOfIntervalWithQtyOfAStore(start, end, noProducts, storeId);

    return getWithSales(results);
  }

  @Override
  public List<StoreWeekTopProductsDto> getTopProductsPerStorePerWeek(
          LocalDateTime start, LocalDateTime end, int noProducts, List<Long> storeIds) {
    List<Map<String, Object>> rows;
    if (storeIds == null || storeIds.isEmpty()) {
      rows = getRepository().findTopProductsPerStorePerWeek(start, end, noProducts);
    } else {
      rows = getRepository().findTopProductsPerStorePerWeekForStores(start, end, noProducts, storeIds);
    }

    // Map (weekStart, storeId) -> list of products
    Map<String, List<ProductGetDto.WithSales>> map = new LinkedHashMap<>();

    for (Map<String, Object> row : rows) {
      // week_start could be java.sql.Date or Timestamp depending on driver
      LocalDate weekStart;
      Object ws = row.get("week_start");
      if (ws instanceof java.sql.Timestamp) {
        weekStart = ((java.sql.Timestamp) ws).toLocalDateTime().toLocalDate();
      } else if (ws instanceof java.sql.Date) {
        weekStart = ((java.sql.Date) ws).toLocalDate();
      } else {
        weekStart = LocalDate.parse(ws.toString());
      }

      Long storeId = ((Number) row.get("store_id")).longValue();
      Long prodId = ((Number) row.get("id")).longValue();
      String sku = (String) row.get("sku");
      String name = (String) row.get("name");
      String description = (String) row.get("description");
      int unitPrice = row.get("unitprice") != null
              ? ((Number) row.get("unitprice")).intValue() : 0;
      Long categoryId = row.get("categoryid") != null ? ((Number) row.get("categoryid")).longValue() : null;
      long totalQty = row.get("totalquantitysold") != null ? ((Number) row.get("totalquantitysold")).longValue() : 0L;

      ProductGetDto.WithSales p = new ProductGetDto.WithSales(prodId, sku, name, description, unitPrice, categoryId, totalQty);

      String key = weekStart.toString() + "|" + storeId;
      map.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
    }

    List<StoreWeekTopProductsDto> result = new ArrayList<>();
    for (Map.Entry<String, List<ProductGetDto.WithSales>> e : map.entrySet()) {
      String[] parts = e.getKey().split("\\|");
      LocalDate weekStart = LocalDate.parse(parts[0]);
      Long storeId = Long.parseLong(parts[1]);
      LocalDate weekEnd = weekStart.plusDays(6);
      result.add(new StoreWeekTopProductsDto(storeId, weekStart, weekEnd, e.getValue()));
    }
    return result;
  }

}
