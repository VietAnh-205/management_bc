package ck4.nvb.rsmanagement.core.module.forecasting.domain.model;

import java.util.List;

public record DailySalesSeries(StoreProductKey key, List<DailySalesPoint> points) {}