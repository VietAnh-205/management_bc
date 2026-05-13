package ck4.nvb.rsmanagement.core.module.stores.product.controller;

import ck4.nvb.rsmanagement.base.application.annotation.RequirePermission;
import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.base.web.controller.api.PageResponse;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.Product;
import ck4.nvb.rsmanagement.core.module.stores.product.service.ProductServiceImpl;
import ck4.nvb.rsmanagement.core.module.stores.product.service.dto.ProductCreateDto;
import ck4.nvb.rsmanagement.core.module.stores.product.service.dto.ProductGetDto;
import ck4.nvb.rsmanagement.core.module.stores.product.service.dto.ProductUpdateDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import java.util.List;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/products")
@Getter
public class ProductController
    extends AuditedCrudController<
        ProductGetDto, Product, Long, UserGetDto, Long, ProductCreateDto, ProductUpdateDto> {

  private final ProductServiceImpl productService;

  public ProductController(ProductServiceImpl productService) {
    super(productService);
    this.productService = productService;
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

  @GetMapping("/qty")
  public ResponseEntity<Integer> remainQuantity(Authentication auth, @RequestParam long productId) {
    UserGetDto user = extractUser(auth);
    return ResponseEntity.ok(getProductService().getRemainQuantity(productId, user.getStoreId()));
  }

  @GetMapping
  @Override
  @RequirePermission(
      value = {"PRODUCT_READ", "FULL_ROLE"},
      logic = RequirePermission.LogicType.ANY)
  public ResponseEntity<ApiResponse<PageResponse<ProductGetDto>>> getList(
      Authentication auth, List<String> query, String sort, int offset, int limit) {
    return super.getList(auth, query, sort, offset, limit);
  }

  @Override
  @PostMapping
  @RequirePermission(
      value = {"PRODUCT_WRITE", "FULL_ROLE"},
      logic = RequirePermission.LogicType.ANY)
  public ResponseEntity<ApiResponse<ProductGetDto>> create(
      Authentication auth, @RequestBody ProductCreateDto productCreateDto) {
    return super.create(auth, productCreateDto);
  }
}
