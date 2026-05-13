package ck4.nvb.rsmanagement.core.module.order.sale_order.controller;

import ck4.nvb.rsmanagement.base.application.annotation.RequirePermission;
import ck4.nvb.rsmanagement.base.application.dto.FilterInput;
import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.base.web.controller.api.PageResponse;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleOrder;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleOrderRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.ISaleLineService;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.ISaleOrderService;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.SaleLineServiceImpl;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.SaleOrderServiceImpl;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto.*;
import ck4.nvb.rsmanagement.core.module.stores.product.service.dto.ProductGetDto;
import ck4.nvb.rsmanagement.core.module.stores.store.service.dto.StoreRevenueSeries;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/orders")
public class SaleOrderController
    extends AuditedCrudController<
        SaleOrderGetFullDto,
        SaleOrder,
        String,
        UserGetDto,
        Long,
        SaleOrderCreateDto,
        SaleOrderUpdateDto> {

  @Autowired private ISaleLineService saleLineService;
  @Autowired private ModelMapper modelMapper;
  @Autowired
  private SaleOrderRepository saleOrderRepository;

  public SaleOrderController(ISaleOrderService service) {
    super(service);
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

  @GetMapping("/most")
  public ResponseEntity<ApiResponse<List<ProductGetDto.WithSales>>> getMostSoldProductsLastDay(
      Authentication auth, @RequestParam int days , @RequestParam int noProducts, @RequestParam long storeId) {
    UserGetDto user = extractUser(auth);
    List<ProductGetDto.WithSales> products =
        saleLineService.getMostSoldProductsLastDayOfAStore(days, noProducts, storeId);
    return ResponseEntity.ok(ApiResponse.success(products));
  }

  @GetMapping("/most-by-week-stores")
  public ResponseEntity<ApiResponse<List<StoreWeekTopProductsDto>>> getTopProductsPerStorePerWeek(
          Authentication auth,
          @RequestParam String start,
          @RequestParam String end,
          @RequestParam int noProducts,
          @RequestParam(required = false) List<Long> storeIds) {

    UserGetDto user = extractUser(auth);

    LocalDateTime startDt = LocalDate.parse(start).atStartOfDay();
    LocalDateTime endDt = LocalDate.parse(end).atTime(LocalTime.MAX);

    List<StoreWeekTopProductsDto> payload =
            saleLineService.getTopProductsPerStorePerWeek(startDt, endDt, noProducts, storeIds);

    return ResponseEntity.ok(ApiResponse.success(payload));
  }


  @PostMapping
  @RequirePermission(
      value = {"ORDER_WRTTE", "FULL_ROLE"},
      logic = RequirePermission.LogicType.ANY)
  @Override
  public ResponseEntity<ApiResponse<SaleOrderGetFullDto>> create(
      Authentication auth, @RequestBody SaleOrderCreateDto entity) {
    UserGetDto user = extractUser(auth);
    SaleOrderGetFullDto output = getService().create(entity, user);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.<SaleOrderGetFullDto>builder()
                .code(201)
                .message("Create order success.")
                .data(output)
                .build());
  }

  @DeleteMapping("/{orderId}")
  @RequirePermission(
      value = {"ORDER_DELETED", "FULL_ROLE"},
      logic = RequirePermission.LogicType.ANY)
  @Override
  public ResponseEntity<ApiResponse<Void>> delete(
      Authentication auth, @PathVariable String orderId) {
    return super.delete(auth, orderId);
  }

  @GetMapping
  @Override
  public ResponseEntity<ApiResponse<PageResponse<SaleOrderGetFullDto>>> getList(
          Authentication auth,
          @RequestParam(required = false, name = "query") List<String> query,
          @RequestParam(required = false, name = "sort") String sort,
          @RequestParam(required = false, name = "offset", defaultValue = "0") int offset,
          @RequestParam(required = false, name = "limit", defaultValue = "20") int limit) {
    return super.getList(auth, query, sort, offset, limit);
  }

  @GetMapping("/{orderId}")
  @Override
  public ResponseEntity<ApiResponse<SaleOrderGetFullDto>> getById(
      Authentication auth, @PathVariable String orderId) {
    return super.getById(auth, orderId);
  }

  @Override
  public ResponseEntity<ApiResponse<PageResponse<SaleOrderGetFullDto>>> getList(
      Authentication auth, FilterInput request) {
    return super.getList(auth, request);
  }

  @GetMapping("/revenue/weeks")
  public ResponseEntity<ApiResponse<List<StoreRevenueSeries>>> getWeeklyRevenueForStores(
          Authentication auth,
          @RequestParam String start, // yyyy-MM-dd
          @RequestParam String end,   // yyyy-MM-dd
          @RequestParam(required = false) List<Long> storeIds) {

    UserGetDto user = extractUser(auth);

    LocalDateTime startDt = LocalDate.parse(start).atStartOfDay();
    LocalDateTime endDt = LocalDate.parse(end).atTime(LocalTime.MAX);

    try {
      List<StoreRevenueSeries> payload =
              ((SaleOrderServiceImpl) getService()).getWeeklyRevenueForStores(startDt, endDt, storeIds);
      return ResponseEntity.ok(ApiResponse.success(payload));
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(ApiResponse.<List<StoreRevenueSeries>>builder()
                      .code(500)
                      .message("Failed to compute weekly revenue: " + ex.getMessage())
                      .data(null)
                      .build());
    }
  }

  @GetMapping("/today")
  public ResponseEntity<ApiResponse<PageResponse<SaleOrderGetFullDto>>> getTodaysOrders(
          Authentication auth,
          @RequestParam(required = false) Long storeId,
          @RequestParam(required = false, name = "sort") String sort,
          @RequestParam(required = false, name = "offset", defaultValue = "0") int offset,
          @RequestParam(required = false, name = "limit", defaultValue = "20") int limit) {

    LocalDate today = LocalDate.now();
    LocalDateTime from = today.atStartOfDay();
    LocalDateTime to = today.plusDays(1).atStartOfDay();

    List<String> query = new ArrayList<>();
    query.add("createdTime>=" + from.toString());
    query.add("createdTime<" + to.toString());
    if (storeId != null) {
      query.add("storeId=" + storeId);
    }

    return getList(auth, query, sort, offset, limit);
  }

}
