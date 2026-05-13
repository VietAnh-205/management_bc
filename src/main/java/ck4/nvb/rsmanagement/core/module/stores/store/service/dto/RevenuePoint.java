package ck4.nvb.rsmanagement.core.module.stores.store.service.dto;

public class RevenuePoint {
  private String date;
  private Long revenue;

  public RevenuePoint() {}

  public RevenuePoint(String date, Long revenue) {
    this.date = date;
    this.revenue = revenue;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public Long getRevenue() {
    return revenue;
  }

  public void setRevenue(Long revenue) {
    this.revenue = revenue;
  }
}
