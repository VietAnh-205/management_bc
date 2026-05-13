package ck4.nvb.rsmanagement.core.module.order.salereturn.controller;

import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.core.module.order.salereturn.domain.SaleReturn;
import ck4.nvb.rsmanagement.core.module.order.salereturn.service.ISaleReturnItemService;
import ck4.nvb.rsmanagement.core.module.order.salereturn.service.ISaleReturnService;
import ck4.nvb.rsmanagement.core.module.order.salereturn.service.dto.SaleReturnCreateDto;
import ck4.nvb.rsmanagement.core.module.order.salereturn.service.dto.SaleReturnGetDto;
import ck4.nvb.rsmanagement.core.module.order.salereturn.service.dto.SaleReturnUpdateDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/return")
public class SaleReturnController
    extends AuditedCrudController<
        SaleReturnGetDto,
        SaleReturn,
        Long,
        UserGetDto,
        Long,
        SaleReturnCreateDto,
        SaleReturnUpdateDto> {

  @Autowired private ISaleReturnItemService saleReturnItemService;

  @Autowired private ModelMapper modelMapper;

  public SaleReturnController(ISaleReturnService service) {
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
