package ck4.nvb.rsmanagement.core.module.users.user.service.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
  @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
  private String passWord;

  @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
  private String fullName;

  @Email(message = "Invalid email format")
  private String email;

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
  private String phone;

  private Long storeId;
}
