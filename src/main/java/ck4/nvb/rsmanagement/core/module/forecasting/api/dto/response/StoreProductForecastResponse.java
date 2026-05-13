package ck4.nvb.rsmanagement.core.module.forecasting.api.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class StoreProductForecastResponse {
  private Long productId;
  private String status;
  private String message;
  private List<ForecastPointResponse> points;
}