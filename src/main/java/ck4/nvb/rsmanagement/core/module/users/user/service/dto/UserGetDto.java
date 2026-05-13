package ck4.nvb.rsmanagement.core.module.users.user.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserGetDto extends BaseUserDto {

  private Long id;

  private String userName;

  private String fullName;

  private String email;

  private String phone;

  private Long storeId;

  private String roleName;

  public UserGetDto(Long id) {
    setId(id);
  }
}
