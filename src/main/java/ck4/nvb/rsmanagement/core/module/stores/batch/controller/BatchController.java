package ck4.nvb.rsmanagement.core.module.stores.batch.controller;

import ck4.nvb.rsmanagement.base.application.annotation.RequirePermission;
import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.Batch;
import ck4.nvb.rsmanagement.core.module.stores.batch.service.IBatchService;
import ck4.nvb.rsmanagement.core.module.stores.batch.service.dto.BatchDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/batch")
public class BatchController
    extends AuditedCrudController<BatchDto, Batch, Long, UserGetDto, Long, BatchDto, BatchDto> {

  protected BatchController(IBatchService batchCrudService) {
    super(batchCrudService);
  }

  @Autowired private IBatchService batchService;

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
  public ResponseEntity<ApiResponse<BatchDto>> create(
      Authentication auth, @RequestBody BatchDto batchDto) {
    return super.create(auth, batchDto);
  }

  @GetMapping("/by-product")
  @RequirePermission(
      value = {"BATCH_ROLE", "FULL_ROLE"},
      logic = RequirePermission.LogicType.ANY)
  public ResponseEntity<ApiResponse<List<BatchDto>>> getByProduct(
      @RequestParam(name = "productId", required = false) String productId,
      @RequestParam(name = "productIds", required = false) String productIds) {

    if (productId != null) {
      try {
        Long id = Long.valueOf(productId);
        List<BatchDto> list = batchService.findByProductId(id);
        return ResponseEntity.ok(ApiResponse.success(list));
      } catch (NumberFormatException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(400, "productId không hợp lệ: " + productId));
      }
    }

    if (productIds != null && !productIds.trim().isEmpty()) {
      try {
        List<Long> ids =
            Arrays.stream(productIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());

        System.out.println("Debug - Parsed productIds: " + ids);
        List<BatchDto> list = batchService.findByProductIds(ids);
        System.out.println("Debug - Found batches: " + list.size());

        return ResponseEntity.ok(ApiResponse.success(list));
      } catch (NumberFormatException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(400, "productIds chứa giá trị không hợp lệ: " + productIds));
      }
    }

    return ResponseEntity.badRequest()
        .body(ApiResponse.error(400, "productId hoặc productIds là bắt buộc"));
  }
}
