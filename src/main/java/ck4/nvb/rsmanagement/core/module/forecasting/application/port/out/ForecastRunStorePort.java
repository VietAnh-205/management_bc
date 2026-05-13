package ck4.nvb.rsmanagement.core.module.forecasting.application.port.out;

import java.time.Instant;
import java.time.LocalDate;

public interface ForecastRunStorePort {
  void saveRun(
      long runId,
      Long storeId,
      String engineType,
      int historyDays,
      int horizonDays,
      int seasonLength,
      Instant generatedAt,
      LocalDate trainFrom,
      LocalDate trainTo);
}