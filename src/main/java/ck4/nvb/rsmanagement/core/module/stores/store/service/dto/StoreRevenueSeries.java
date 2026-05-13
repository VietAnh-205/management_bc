package ck4.nvb.rsmanagement.core.module.stores.store.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
@Data

public class StoreRevenueSeries {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long storeId;
  private String storeName;
  private List<RevenuePoint> series;

  public StoreRevenueSeries() {}

  public StoreRevenueSeries(Long storeId, String storeName, List<RevenuePoint> series) {
    this.storeId = storeId;
    this.storeName = storeName;
    this.series = series;
  }
}
