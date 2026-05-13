package ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.controller;

import ck4.nvb.rsmanagement.base.web.controller.AuditedCrudController;
import ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.domain.InventoryAdjustment;
import ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.service.InventoryAdjustmentServiceImpl;
import ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.service.dto.InventoryAdjustmentDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/inventory_adjustment")
public class InventoryAdjustmentController
    extends AuditedCrudController<
        InventoryAdjustmentDto,
        InventoryAdjustment,
        Long,
        UserGetDto,
        Long,
        InventoryAdjustmentDto,
        InventoryAdjustmentDto> {

  public InventoryAdjustmentController(InventoryAdjustmentServiceImpl service) {
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
      return new ModelMapper().map(principal, UserGetDto.class);
    }

    return null;
  }
}
