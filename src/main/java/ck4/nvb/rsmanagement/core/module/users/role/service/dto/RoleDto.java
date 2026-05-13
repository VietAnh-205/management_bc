package ck4.nvb.rsmanagement.core.module.users.role.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDto extends EntityDto<Long> {
  private String name;
  private String desc;
}
