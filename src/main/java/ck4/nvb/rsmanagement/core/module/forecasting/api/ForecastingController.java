package ck4.nvb.rsmanagement.core.module.forecasting.api;

import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.core.module.forecasting.api.dto.request.StoreBatchForecastRequest;
import ck4.nvb.rsmanagement.core.module.forecasting.api.dto.response.StoreBatchForecastResponse;
import ck4.nvb.rsmanagement.core.module.forecasting.application.service.StoreBatchForecastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/forecasting")
@RequiredArgsConstructor
public class ForecastingController {

  private final StoreBatchForecastService storeBatchForecastService;

  @PostMapping("/stores/{storeId}/forecasts:batch")
  public ResponseEntity<ApiResponse<StoreBatchForecastResponse>> batchForecast(
      @PathVariable Long storeId, @Valid @RequestBody StoreBatchForecastRequest request) {

    StoreBatchForecastResponse response = storeBatchForecastService.forecastNextWeek(storeId, request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}