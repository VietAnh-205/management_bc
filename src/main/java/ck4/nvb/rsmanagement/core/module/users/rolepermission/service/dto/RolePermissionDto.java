package ck4.nvb.rsmanagement.core.module.users.rolepermission.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePermissionDto extends EntityDto<Long> {
  private Long roleId;
  private Long permissionId;
}
