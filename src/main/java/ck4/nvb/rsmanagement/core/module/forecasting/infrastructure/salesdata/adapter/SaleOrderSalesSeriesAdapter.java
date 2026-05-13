package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.salesdata.adapter;

import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.StoreSalesSeriesPort;
import ck4.nvb.rsmanagement.core.module.forecasting.domain.model.StoreProductKey;
import ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.salesdata.projection.DailyStoreProductSalesRow;
import ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.salesdata.repository.ForecastSalesQueryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaleOrderSalesSeriesAdapter implements StoreSalesSeriesPort {

  private final ForecastSalesQueryRepository forecastSalesQueryRepository;

  @Override
  public Map<StoreProductKey, Map<LocalDate, Integer>> loadDailySales(
      Long storeId, List<Long> productIds, LocalDate from, LocalDate to) {

    LocalDateTime fromInclusive = from.atStartOfDay();
    LocalDateTime toInclusive = to.atTime(LocalTime.MAX); // include whole "today"

    List<DailyStoreProductSalesRow> rows =
        forecastSalesQueryRepository.findDailySalesByStoreAndProducts(
            storeId, productIds, fromInclusive, toInclusive);

    Map<StoreProductKey, Map<LocalDate, Integer>> out = new HashMap<>();
    for (DailyStoreProductSalesRow row : rows) {
      StoreProductKey key = new StoreProductKey(row.getStoreId(), row.getProductId());
      out.computeIfAbsent(key, k -> new HashMap<>())
          .put(row.getSaleDate(), row.getQty() == null ? 0 : row.getQty());
    }
    return out;
  }
}