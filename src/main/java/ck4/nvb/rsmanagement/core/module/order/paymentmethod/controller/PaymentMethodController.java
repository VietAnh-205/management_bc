package ck4.nvb.rsmanagement.core.module.order.paymentmethod.controller;

import ck4.nvb.rsmanagement.base.application.annotation.RequirePermission;
import ck4.nvb.rsmanagement.base.application.dto.FilterInput;
import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.base.web.controller.api.PageResponse;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain.PaymentMethod;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.service.IPaymentService;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.service.PaymentMethodServiceImpl;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.service.dto.PaymentMethodDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/payment-method")
public class PaymentMethodController
    extends AuditedCrudController<
        PaymentMethodDto,
        PaymentMethod,
        Long,
        UserGetDto,
        Long,
        PaymentMethodDto,
        PaymentMethodDto> {

  @Autowired
  public PaymentMethodController(PaymentMethodServiceImpl service) {
    super(service);
  }

  @Autowired private IPaymentService service;

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

  @PostMapping
  @Override
  public ResponseEntity<ApiResponse<PaymentMethodDto>> create(
      Authentication auth, @RequestBody PaymentMethodDto entity) {
    return super.create(auth, entity);
  }

  @PutMapping("/{paymentMethodId}")
  @RequirePermission(
      value = {"PAYTMENT_ROLE", "FULL_ROlLE"},
      logic = RequirePermission.LogicType.ANY)
  @Override
  public ResponseEntity<ApiResponse<PaymentMethodDto>> update(
      Authentication auth,
      @PathVariable Long paymentMethodId,
      @RequestBody PaymentMethodDto entity) {
    return super.update(auth, paymentMethodId, entity);
  }

  @DeleteMapping("/{paymentMethodId}")
  @RequirePermission(
      value = {"PAYTMENT_ROLE", "FULL_ROlLE"},
      logic = RequirePermission.LogicType.ANY)
  @Override
  public ResponseEntity<ApiResponse<Void>> delete(
      Authentication auth, @PathVariable Long paymentMethodId) {
    return super.delete(auth, paymentMethodId);
  }

  @GetMapping
  @Override
  public ResponseEntity<ApiResponse<PageResponse<PaymentMethodDto>>> getList(
      Authentication auth,
      @RequestParam(required = false, name = "query") List<String> query,
      @RequestParam(required = false, name = "sort") String sort,
      @RequestParam(required = false, name = "offset", defaultValue = "0") int offset,
      @RequestParam(required = false, name = "limit", defaultValue = "20") int limit) {
    return super.getList(auth, query, sort, offset, limit);
  }

  @GetMapping("/{paymentMethodId}")
  @Override
  public ResponseEntity<ApiResponse<PaymentMethodDto>> getById(
      Authentication auth, @PathVariable Long paymentMethodId) {
    return super.getById(auth, paymentMethodId);
  }

  @Override
  public ResponseEntity<ApiResponse<PageResponse<PaymentMethodDto>>> getList(
      Authentication auth, FilterInput request) {
    return super.getList(auth, request);
  }

  //  @GetMapping("/usage")
  //  public ResponseEntity<ApiResponse<PageResponse<PaymentMethodGetDto.WithUsageStats>>>
  // getUsageStats(Authentication auth, @RequestParam int days) {
  //    UserGetDto user = extractUser(auth);
  //
  //    List<PaymentMethodGetDto.WithUsageStats> output = service.getUsageStatsOfInterval(days);
  //
  //    return APIResponseBuilder.successList(output, 0, output.size(), output.size(), "get usage
  // stats successfully");
  //  }
}
