package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.salesdata.adapter;

import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.ForecastPointStorePort;
import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.IdGeneratorPort;
import ck4.nvb.rsmanagement.core.module.forecasting.domain.model.ForecastPoint;
import ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.persistence.entity.ForecastPointEntity;
import ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.persistence.repository.ForecastPointRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaForecastPointStoreAdapter implements ForecastPointStorePort {

  private final ForecastPointRepository forecastPointRepository;
  private final IdGeneratorPort idGeneratorPort;

  @Override
  public void savePoints(long runId, Long storeId, Long productId, List<ForecastPoint> points) {
    if (points == null || points.isEmpty()) return;

    List<ForecastPointEntity> entities = new ArrayList<>(points.size());
    for (ForecastPoint p : points) {
      ForecastPointEntity e = new ForecastPointEntity();
      e.setId(idGeneratorPort.nextId());
      e.setRunId(runId);
      e.setStoreId(storeId);
      e.setProductId(productId);
      e.setForecastDate(p.date());
      e.setYhat(p.yhat());
      entities.add(e);
    }

    forecastPointRepository.saveAll(entities);
  }
}