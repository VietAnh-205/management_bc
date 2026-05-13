package ck4.nvb.rsmanagement.core.module.order.sale_order.controller;

import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleAllocation;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.ISaleAllocationService;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto.SaleAllocationDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/sale-allocation")
public class SaleAllocationController
    extends AuditedCrudController<
        SaleAllocationDto,
        SaleAllocation,
        Long,
        UserGetDto,
        Long,
        SaleAllocationDto,
        SaleAllocationDto> {

  @Autowired private ModelMapper modelMapper;

  public SaleAllocationController(ISaleAllocationService service) {
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
}
