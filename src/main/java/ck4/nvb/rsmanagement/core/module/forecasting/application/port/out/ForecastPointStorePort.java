package ck4.nvb.rsmanagement.core.module.forecasting.application.port.out;

import ck4.nvb.rsmanagement.core.module.forecasting.domain.model.ForecastPoint;
import java.util.List;

public interface ForecastPointStorePort {
  void savePoints(long runId, Long storeId, Long productId, List<ForecastPoint> points);
}