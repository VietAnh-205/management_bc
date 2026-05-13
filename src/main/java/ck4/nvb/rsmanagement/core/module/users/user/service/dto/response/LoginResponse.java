package ck4.nvb.rsmanagement.core.module.users.user.service.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
  private Long id;
  private String userName;
  private String fullName;
  private String email;
  private String phone;
  private Long storeId;
  private LocalDateTime lastLogin;
  private String token;
  private List<String> roles;
  private List<String> permissions;
}
