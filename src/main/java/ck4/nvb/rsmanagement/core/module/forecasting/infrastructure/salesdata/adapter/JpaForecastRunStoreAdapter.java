package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.salesdata.adapter;

import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.ForecastRunStorePort;
import ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.persistence.entity.ForecastRunEntity;
import ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.persistence.repository.ForecastRunRepository;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaForecastRunStoreAdapter implements ForecastRunStorePort {

  private final ForecastRunRepository forecastRunRepository;

  @Override
  public void saveRun(
      long runId,
      Long storeId,
      String engineType,
      int historyDays,
      int horizonDays,
      int seasonLength,
      Instant generatedAt,
      LocalDate trainFrom,
      LocalDate trainTo) {

    ForecastRunEntity e = new ForecastRunEntity();
    e.setId(runId);
    e.setStoreId(storeId);
    e.setEngineType(engineType);
    e.setHistoryDays(historyDays);
    e.setHorizonDays(horizonDays);
    e.setSeasonLength(seasonLength);
    e.setGeneratedAt(generatedAt);
    e.setTrainFrom(trainFrom);
    e.setTrainTo(trainTo);

    forecastRunRepository.save(e);
  }
}