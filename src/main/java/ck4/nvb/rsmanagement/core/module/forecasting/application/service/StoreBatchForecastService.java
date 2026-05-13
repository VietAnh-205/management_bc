package ck4.nvb.rsmanagement.core.module.forecasting.application.service;

import ck4.nvb.rsmanagement.core.module.forecasting.api.dto.request.StoreBatchForecastRequest;
import ck4.nvb.rsmanagement.core.module.forecasting.api.dto.response.StoreBatchForecastResponse;

public interface StoreBatchForecastService {
  StoreBatchForecastResponse forecastNextWeek(Long storeId, StoreBatchForecastRequest request);
}