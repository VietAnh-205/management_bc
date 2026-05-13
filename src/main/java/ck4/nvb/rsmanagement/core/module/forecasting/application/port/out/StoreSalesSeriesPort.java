package ck4.nvb.rsmanagement.core.module.forecasting.application.port.out;

import ck4.nvb.rsmanagement.core.module.forecasting.domain.model.StoreProductKey;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StoreSalesSeriesPort {

  /**
   * @return map (storeId, productId) -> map date -> qtySum (int)
   */
  Map<StoreProductKey, Map<LocalDate, Integer>> loadDailySales(
      Long storeId, List<Long> productIds, LocalDate from, LocalDate to);
}