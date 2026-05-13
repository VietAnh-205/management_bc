package ck4.nvb.rsmanagement.core.module.order.report;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductWeeklySalesDto {
  public Long storeId;
  public Long productId;
  public LocalDateTime weekStart;
  public Long qty;
  public Long revenue;
}
