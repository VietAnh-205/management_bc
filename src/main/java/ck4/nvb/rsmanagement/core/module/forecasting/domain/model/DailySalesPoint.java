package ck4.nvb.rsmanagement.core.module.forecasting.domain.model;

import java.time.LocalDate;

public record DailySalesPoint(LocalDate date, int qty) {}