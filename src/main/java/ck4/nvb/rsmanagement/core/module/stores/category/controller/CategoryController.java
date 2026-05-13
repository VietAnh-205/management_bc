package ck4.nvb.rsmanagement.core.module.stores.category.controller;

import ck4.nvb.rsmanagement.base.application.annotation.RequirePermission;
import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.core.module.stores.category.domain.Category;
import ck4.nvb.rsmanagement.core.module.stores.category.service.CategoryServiceImpl;
import ck4.nvb.rsmanagement.core.module.stores.category.service.dto.CategoryDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/categories")
public class CategoryController
    extends AuditedCrudController<
        CategoryDto, Category, Long, UserGetDto, Long, CategoryDto, CategoryDto> {

  protected CategoryController(CategoryServiceImpl categoryService) {
    super(categoryService);
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
      value = {"CATEGORY_ROLE", "FULL_ROLE"},
      logic = RequirePermission.LogicType.ANY)
  public ResponseEntity<ApiResponse<CategoryDto>> create(
      Authentication auth, @RequestBody CategoryDto categoryDto) {
    return super.create(auth, categoryDto);
  }
}
