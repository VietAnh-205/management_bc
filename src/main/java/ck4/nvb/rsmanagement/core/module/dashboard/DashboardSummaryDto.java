package ck4.nvb.rsmanagement.core.module.dashboard;

public record DashboardSummaryDto(
    int totalProducts, int todayOrders, long monthlyRevenue, int totalCustomer) {}
