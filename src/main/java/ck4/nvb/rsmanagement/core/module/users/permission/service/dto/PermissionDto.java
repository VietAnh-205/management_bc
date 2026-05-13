package ck4.nvb.rsmanagement.core.module.users.permission.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionDto extends EntityDto<Long> {
  private String code;
  private String description;
}
