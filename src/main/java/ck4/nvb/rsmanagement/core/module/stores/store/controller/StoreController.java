package ck4.nvb.rsmanagement.core.module.stores.store.controller;

import ck4.nvb.rsmanagement.base.application.dto.FilterInput;
import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.base.web.controller.api.PageResponse;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.SaleOrderServiceImpl;
import ck4.nvb.rsmanagement.core.module.stores.store.domain.Store;
import ck4.nvb.rsmanagement.core.module.stores.store.service.StoreCrudServiceImpl;
import ck4.nvb.rsmanagement.core.module.stores.store.service.dto.StoreDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${rs.api.main.baseUrl}/store")
public class StoreController
    extends AuditedCrudController<StoreDto, Store, Long, UserGetDto, Long, StoreDto, StoreDto> {

  @Autowired private SaleOrderServiceImpl saleOrderService;

  public StoreController(StoreCrudServiceImpl storeCrudService) {
    super(storeCrudService);
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
      return new ModelMapper().map(principal, UserGetDto.class);
    }

    return null;
  }

  @PostMapping
  @Override
  public ResponseEntity<ApiResponse<StoreDto>> create(
      Authentication auth, @RequestBody StoreDto entity) {
    return super.create(auth, entity);
  }

  @PutMapping("/{storeId}")
  @Override
  public ResponseEntity<ApiResponse<StoreDto>> update(
      Authentication auth, @PathVariable Long storeId, @RequestBody StoreDto entity) {
    return super.update(auth, storeId, entity);
  }

  @DeleteMapping("/{storeId}")
  @Override
  public ResponseEntity<ApiResponse<Void>> delete(Authentication auth, @PathVariable Long storeId) {
    return super.delete(auth, storeId);
  }

  @GetMapping
  @Override
  public ResponseEntity<ApiResponse<PageResponse<StoreDto>>> getList(
      Authentication auth,
      @RequestParam(required = false, name = "query") List<String> query,
      @RequestParam(required = false, name = "sort") String sort,
      @RequestParam(required = false, name = "offset", defaultValue = "0") int offset,
      @RequestParam(required = false, name = "limit", defaultValue = "20") int limit) {
    return super.getList(auth, query, sort, offset, limit);
  }

  @GetMapping("/{supplierId}")
  @Override
  public ResponseEntity<ApiResponse<StoreDto>> getById(
      Authentication auth, @PathVariable Long storeId) {
    return super.getById(auth, storeId);
  }

  @Override
  public ResponseEntity<ApiResponse<PageResponse<StoreDto>>> getList(
      Authentication auth, FilterInput request) {
    return super.getList(auth, request);
  }

  @GetMapping("/{storeId}/revenue")
  public ResponseEntity<ApiResponse<Object>> getStoreRevenueSeries(
      @PathVariable Long storeId, @RequestParam(required = false, defaultValue = "30") int days) {

    try {
      Object resp = saleOrderService.getAStoreRevenueSeries(storeId, days);
      return ResponseEntity.ok(ApiResponse.success(resp));
    } catch (Exception ex) {
      ex.printStackTrace();
      return ResponseEntity.status(500)
          .body(
              ApiResponse.<Object>builder()
                  .code(500)
                  .message("Failed to get revenue")
                  .data(null)
                  .build());
    }
  }

  @GetMapping("/revenue/all")
  public ResponseEntity<ApiResponse<Object>> getAllStoresRevenueSeries(
      @RequestParam(required = false, defaultValue = "30") int days) {
    try {
      Object resp = saleOrderService.getAllStoresRevenueSeries(days);
      return ResponseEntity.ok(ApiResponse.success(resp));
    } catch (Exception ex) {
      ex.printStackTrace();
      return ResponseEntity.status(500)
          .body(
              ApiResponse.<Object>builder()
                  .code(500)
                  .message("Failed to get all stores revenue")
                  .data(null)
                  .build());
    }
  }
}
