package ck4.nvb.rsmanagement.core.module.stores.batch_stock.controller;

import ck4.nvb.rsmanagement.base.application.annotation.RequirePermission;
import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStock;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.service.BatchStockServiceImpl;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.service.dto.BatchStockDto;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.service.dto.BatchStockGetDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/batch-stocks")
public class BatchStockController
    extends AuditedCrudController<
        BatchStockDto, BatchStock, Long, UserGetDto, Long, BatchStockDto, BatchStockDto> {

  public BatchStockController(BatchStockServiceImpl importLogService) {
    super(importLogService);
  }

  @Override
  public UserGetDto extractUser(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
      return null;
    }

    Object principal = auth.getPrincipal();
    if (principal instanceof UserGetDto) {
      return (UserGetDto) principal;
    }

    if (principal instanceof UserRoleDto) {
      UserRoleDto userRoleDto = (UserRoleDto) principal;
      UserGetDto userGetDto = new UserGetDto();
      userGetDto.setId(userRoleDto.getUserId());
      userGetDto.setUserName(userRoleDto.getUserName());
      return userGetDto;
    }
    return null;
  }

  @Override
  @PostMapping
  @RequirePermission(
      value = {"BATCH_ROLE", "FULL_ROLE"},
      logic = RequirePermission.LogicType.ANY)
  public ResponseEntity<ApiResponse<BatchStockDto>> create(
      Authentication auth, @RequestBody BatchStockDto batchStockDto) {
    return super.create(auth, batchStockDto);
  }

  @GetMapping("/available")
  public ResponseEntity<ApiResponse<List<BatchStockGetDto>>> getAvailable(
      @RequestParam("productId") Long productId, @RequestParam("storeId") Long storeId) {
    List<BatchStockGetDto> list =
        ((BatchStockServiceImpl) getService())
            .getAvailableBatchInfoByProductAndStore(productId, storeId);
    return ResponseEntity.ok(ApiResponse.success(list));
  }

  @GetMapping("/available-total")
  public ResponseEntity<ApiResponse<Long>> getAvailableTotal(
      @RequestParam("productId") Long productId, @RequestParam("storeId") Long storeId) {
    Long total =
        ((BatchStockServiceImpl) getService())
            .getTotalAvailableQuantityByProductAndStore(productId, storeId);
    return ResponseEntity.ok(ApiResponse.success(total));
  }
}
