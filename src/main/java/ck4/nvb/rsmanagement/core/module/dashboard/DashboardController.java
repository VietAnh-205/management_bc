package ck4.nvb.rsmanagement.core.module.dashboard;

import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/summary")
@Getter
@RequiredArgsConstructor
public class DashboardController {
  private final DashboardServiceImpl dashboardService;

  @GetMapping
  public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardSummary() {
    DashboardSummaryDto dto = dashboardService.getDashboardSummary();
    return ResponseEntity.ok(ApiResponse.success(dto));
  }
}
