package ck4.nvb.rsmanagement.core.module.forecasting.domain.model;

import java.time.LocalDate;

public record ForecastPoint(LocalDate date, double yhat) {}