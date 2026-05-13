package ck4.nvb.rsmanagement.core.module.forecasting.application.service;

import ck4.nvb.rsmanagement.core.module.forecasting.api.dto.request.StoreBatchForecastRequest;
import ck4.nvb.rsmanagement.core.module.forecasting.api.dto.response.ForecastPointResponse;
import ck4.nvb.rsmanagement.core.module.forecasting.api.dto.response.StoreBatchForecastResponse;
import ck4.nvb.rsmanagement.core.module.forecasting.api.dto.response.StoreProductForecastResponse;
import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.ClockPort;
import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.ForecastPointStorePort;
import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.ForecastRunStorePort;
import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.IdGeneratorPort;
import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.PythonForecastingPort;
import ck4.nvb.rsmanagement.core.module.forecasting.application.port.out.StoreSalesSeriesPort;
import ck4.nvb.rsmanagement.core.module.forecasting.domain.model.ForecastPoint;
import ck4.nvb.rsmanagement.core.module.forecasting.domain.model.StoreProductKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoreBatchForecastServiceImpl implements StoreBatchForecastService {

    private final StoreSalesSeriesPort storeSalesSeriesPort;
    private final ForecastRunStorePort forecastRunStorePort;
    private final ForecastPointStorePort forecastPointStorePort;
    private final PythonForecastingPort pythonForecastingPort;
    private final IdGeneratorPort idGeneratorPort;
    private final ClockPort clockPort;

    @Value("${forecasting.historyDays:180}")
    private int historyDays;

    @Value("${forecasting.horizonDays:7}")
    private int horizonDays;

    @Value("${forecasting.seasonLength:7}")
    private int seasonLength;

    @Override
    @Transactional
    public StoreBatchForecastResponse forecastNextWeek(Long storeId, StoreBatchForecastRequest request) {
        // Fix 1: Sử dụng .today() của ClockPort
        LocalDate today = clockPort.today();
        LocalDate fromDate = today.minusDays(historyDays);
        LocalDate toDate = today.minusDays(1);

        // Fix 2: Sử dụng .nextId() của IdGeneratorPort
        long runId = idGeneratorPort.nextId();

        // Lấy dữ liệu bán hàng từ DB
        Map<StoreProductKey, Map<LocalDate, Integer>> salesData =
                storeSalesSeriesPort.loadDailySales(storeId, request.getProductIds(), fromDate, toDate);

        List<StoreProductForecastResponse> productResponses = new ArrayList<>();

        for (Long productId : request.getProductIds()) {
            StoreProductKey key = new StoreProductKey(storeId, productId);
            Map<LocalDate, Integer> productSales = salesData.getOrDefault(key, Collections.emptyMap());

            StoreProductForecastResponse productResponse = new StoreProductForecastResponse();
            productResponse.setProductId(productId);

            if (productSales.isEmpty()) {
                productResponse.setStatus("SKIPPED");
                productResponse.setMessage("No historical data");
                productResponse.setPoints(Collections.emptyList());
                productResponses.add(productResponse);
                continue;
            }

            try {
                // Bắn Request HTTP sang Python
                List<ForecastPoint> predictedPoints = pythonForecastingPort.requestForecast(
                        request.getEngineType().name(),
                        productId,
                        productSales,
                        horizonDays
                );

                // Lưu kết quả vào DB
                forecastPointStorePort.savePoints(runId, storeId, productId, predictedPoints);

                // Fix 4: Constructor của ForecastPointResponse yêu cầu 2 tham số, và cần ép kiểu về int
                List<ForecastPointResponse> pointResponses = new ArrayList<>();
                for (ForecastPoint pt : predictedPoints) {
                    // Ép kiểu double yhat của domain model thành Integer yhat của DTO
                    int roundedYhat = (int) Math.round(pt.yhat());

                    // ForecastPointResponse sử dụng @AllArgsConstructor nên phải dùng constructor này
                    pointResponses.add(new ForecastPointResponse(pt.date(), roundedYhat));
                }

                productResponse.setStatus("SUCCESS");
                productResponse.setPoints(pointResponses);
            } catch (Exception e) {
                productResponse.setStatus("FAILED");
                productResponse.setMessage("Python Service Error: " + e.getMessage());
                productResponse.setPoints(Collections.emptyList());
            }

            productResponses.add(productResponse);
        }

        // Fix 3: Truyền đúng 9 tham số cho hàm saveRun
        forecastRunStorePort.saveRun(
                runId,
                storeId,
                request.getEngineType().name(),
                historyDays,
                horizonDays,
                seasonLength,
                clockPort.nowInstant(),
                fromDate,
                toDate
        );

        // Build Final Response
        StoreBatchForecastResponse response = new StoreBatchForecastResponse();
        response.setRunId(runId);
        response.setStoreId(storeId);
        response.setEngineType(request.getEngineType().name());
        response.setHistoryDays(historyDays);
        response.setHorizonDays(horizonDays);

        // Sử dụng .nowInstant() theo ClockPort
        response.setGeneratedAt(clockPort.nowInstant());

        response.setResults(productResponses);

        return response;
    }
}