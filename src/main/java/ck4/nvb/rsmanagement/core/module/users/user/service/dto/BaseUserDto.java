package ck4.nvb.rsmanagement.core.module.users.user.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseUserDto extends EntityDto<Long> {
  private String userName;
  private String fullName;
  private String email;
  private String phone;
  private Long storeId;
}
