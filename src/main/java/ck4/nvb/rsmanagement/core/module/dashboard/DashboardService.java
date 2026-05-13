package ck4.nvb.rsmanagement.core.module.dashboard;

import ck4.nvb.rsmanagement.base.application.exception.AppException;

public interface DashboardService {
  DashboardSummaryDto getDashboardSummary() throws AppException;
}
