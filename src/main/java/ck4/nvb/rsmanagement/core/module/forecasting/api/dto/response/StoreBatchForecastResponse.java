package ck4.nvb.rsmanagement.core.module.forecasting.api.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class StoreBatchForecastResponse {
  private Long runId;
  private Long storeId;
  private String engineType;
  private int historyDays;
  private int horizonDays;
  private Instant generatedAt;

  private List<StoreProductForecastResponse> results;
}