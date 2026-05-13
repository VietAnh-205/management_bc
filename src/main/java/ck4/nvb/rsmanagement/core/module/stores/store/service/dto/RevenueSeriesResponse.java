package ck4.nvb.rsmanagement.core.module.stores.store.service.dto;

import lombok.Data;

import java.util.List;
@Data

public class RevenueSeriesResponse {
  private List<RevenuePoint> series;
  private Long day;
  private Long week;
  private Long month;

  public RevenueSeriesResponse() {}

  public RevenueSeriesResponse(List<RevenuePoint> series, Long day, Long week, Long month) {
    this.series = series;
    this.day = day;
    this.week = week;
    this.month = month;
  }
}
