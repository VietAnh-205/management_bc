package ck4.nvb.rsmanagement.core.module.stores.transfer.controller;

import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.core.module.stores.transfer.domain.StoreTransfer;
import ck4.nvb.rsmanagement.core.module.stores.transfer.service.IStoreTransferService;
import ck4.nvb.rsmanagement.core.module.stores.transfer.service.dto.StoreTransferDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/store-transfers")
public class StoreTransferController
    extends AuditedCrudController<
        StoreTransferDto,
        StoreTransfer,
        Long,
        UserGetDto,
        Long,
        StoreTransferDto,
        StoreTransferDto> {

  public StoreTransferController(IStoreTransferService service) {
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

    if (principal instanceof UserRoleDto userRoleDto) {
      UserGetDto userGetDto = new UserGetDto();
      userGetDto.setId(userRoleDto.getUserId());
      userGetDto.setUserName(userRoleDto.getUserName());
      return userGetDto;
    }
    return null;
  }
}
