package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.salesdata.projection;

import java.time.LocalDate;

public interface DailyStoreProductSalesRow {
  Long getStoreId();
  Long getProductId();
  LocalDate getSaleDate();
  Integer getQty();
}