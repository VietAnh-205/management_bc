package ck4.nvb.rsmanagement.core.module.order.customer.controller;

import ck4.nvb.rsmanagement.base.application.annotation.RequirePermission;
import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.core.module.order.customer.domain.Customer;
import ck4.nvb.rsmanagement.core.module.order.customer.service.CustomerCrudServiceImpl;
import ck4.nvb.rsmanagement.core.module.order.customer.service.dto.CustomerDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/customer")
public class CustomerController
    extends AuditedCrudController<
        CustomerDto, Customer, Long, UserGetDto, Long, CustomerDto, CustomerDto> {

  @Autowired
  public CustomerController(CustomerCrudServiceImpl customerCrudService) {
    super(customerCrudService);
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
      value = {"CUSTOMER_ROLE", "FULL_ROLE"},
      logic = RequirePermission.LogicType.ANY)
  public ResponseEntity<ApiResponse<CustomerDto>> create(
      Authentication auth, @RequestBody CustomerDto customerDto) {
    return super.create(auth, customerDto);
  }
}
