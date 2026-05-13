package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.engine;

import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.PythonForecastingPort;
import ck4.nvb.rsmanagement.core.module.forecasting.domain.model.ForecastPoint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HttpPythonForecastingAdapter implements PythonForecastingPort {

    private final RestTemplate restTemplate;

    @Value("${python.forecasting.url:http://localhost:8000/api/v1/forecast}")
    private String pythonServiceUrl;

    public HttpPythonForecastingAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<ForecastPoint> requestForecast(String engineType, Long productId, Map<LocalDate, Integer> historicalData, int forecastDays) {

        PythonForecastRequest request = new PythonForecastRequest();
        request.setAlgorithm(engineType);
        request.setProductId(productId);
        request.setForecastDays(forecastDays);

        List<HistoricalRecord> historyRecords = new ArrayList<>();
        historicalData.forEach((date, quantity) -> {
            historyRecords.add(new HistoricalRecord(date, quantity));
        });
        request.setHistoryData(historyRecords);

        // Bắn API
        PythonForecastResponse response = restTemplate.postForObject(
                pythonServiceUrl,
                request,
                PythonForecastResponse.class
        );

        if (response == null || response.getPoints() == null) {
            return List.of();
        }

        List<ForecastPoint> forecastPoints = new ArrayList<>();
        for (PythonForecastPoint pt : response.getPoints()) {
            // ForecastPoint là record nên truyền constructor như thế này
            forecastPoints.add(new ForecastPoint(pt.getDate(), pt.getYhat()));
        }

        return forecastPoints;
    }

    // ==============================================================
    // INNER CLASSES DTO -DÙNG ĐỂ ÁNH XẠ JSON SANG PYTHON
    // ==============================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PythonForecastRequest {
        private String algorithm;
        private Long productId;
        private int forecastDays;
        private List<HistoricalRecord> historyData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalRecord {
        private LocalDate date;
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PythonForecastResponse {
        private List<PythonForecastPoint> points;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PythonForecastPoint {
        private LocalDate date;
        private double yhat;
    }
}