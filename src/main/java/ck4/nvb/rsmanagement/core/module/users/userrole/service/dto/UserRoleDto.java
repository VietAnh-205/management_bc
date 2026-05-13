package ck4.nvb.rsmanagement.core.module.users.userrole.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleDto extends EntityDto<Long> {
  // main relationships
  private Long userId;
  private Long roleId;
  private Long storeId;

  // user information (optional)
  private String userName;
  private String fullName;
  private String email;
  private String phone;

  // role information
  private String roleName;

  // permissions for role
  private List<String> permissions;

  // THÊM session fields từ UserSessionDto
  private String currentStoreName;
  private String ipAddress;
  private String deviceSession;
  private String traceId;

  // Helper method
  public UserGetDto toUserGetDto() {
    UserGetDto dto = new UserGetDto();
    dto.setId(userId);
    dto.setUserName(userName);
    dto.setFullName(fullName);
    dto.setEmail(email);
    dto.setPhone(phone);
    dto.setStoreId(storeId);
    return dto;
  }
}
