package ck4.nvb.rsmanagement.core.module.forecasting.application.port.out;

import ck4.nvb.rsmanagement.core.module.forecasting.domain.model.ForecastPoint;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PythonForecastingPort {
    // Truyền dữ liệu từng sản phẩm sang Python, nhận về List điểm dự báo
    List<ForecastPoint> requestForecast(
            String engineType,
            Long productId,
            Map<LocalDate, Integer> historicalData,
            int forecastDays
    );
}