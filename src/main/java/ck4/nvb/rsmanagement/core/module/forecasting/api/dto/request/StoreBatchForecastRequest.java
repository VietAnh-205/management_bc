package ck4.nvb.rsmanagement.core.module.forecasting.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class StoreBatchForecastRequest {

  @NotEmpty private List<Long> productIds;

  @NotNull private EngineType engineType;

  public enum EngineType {
    ETS,
    SARIMA
  }
}