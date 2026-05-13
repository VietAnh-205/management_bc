package ck4.nvb.rsmanagement.core.module.stores.store.service.dto;

import lombok.Data;

import java.util.List;
@Data

public class AllStoresRevenueResponse {
  private List<StoreRevenueSeries> stores;
  private Long dayTotal;
  private Long weekTotal;
  private Long monthTotal;
  private String from;
  private String to;

  public AllStoresRevenueResponse() {}

  public AllStoresRevenueResponse(
      List<StoreRevenueSeries> stores,
      Long dayTotal,
      Long weekTotal,
      Long monthTotal,
      String from,
      String to) {
    this.stores = stores;
    this.dayTotal = dayTotal;
    this.weekTotal = weekTotal;
    this.monthTotal = monthTotal;
    this.from = from;
    this.to = to;
  }
}
