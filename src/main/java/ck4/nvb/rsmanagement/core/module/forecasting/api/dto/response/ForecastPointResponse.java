package ck4.nvb.rsmanagement.core.module.forecasting.api.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForecastPointResponse {
  private LocalDate date;
  private Integer yhat; // rounded
}