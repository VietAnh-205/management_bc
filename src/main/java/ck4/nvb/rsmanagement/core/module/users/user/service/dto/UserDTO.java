package ck4.nvb.rsmanagement.core.module.users.user.service.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends BaseDTO {
  private Long id;
  private String userName;
  private String fullName;
  private String email;
  private String phone;
  private Long storeId;
  private LocalDateTime lastLogin;
  private List<String> roles;
  private List<String> permissions;
}
