package ck4.nvb.rsmanagement.core.module.users.user.controller;

import ck4.nvb.rsmanagement.base.application.annotation.RequirePermission;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserDTO;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.request.CreateUserRequest;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.request.UpdateUserRequest;
import ck4.nvb.rsmanagement.core.module.users.user.service.impl.UserGetServiceWithRoleImpl;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${rs.api.main.baseUrl}/users/with-permissions")
@RequiredArgsConstructor
@Slf4j
public class UserControllerWithPermissions {

  private final UserGetServiceWithRoleImpl userService;

  @PostMapping
  @RequirePermission("USER_WRITE")
  public ResponseEntity<ApiResponse<UserDTO>> createUser(
      @RequestBody @Valid CreateUserRequest request) {
    try {
      UserDTO user = userService.createUser(request);
      return ResponseEntity.ok(ApiResponse.success(user));
    } catch (Exception e) {
      log.error("Create user failed", e);
      return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
    }
  }

  @GetMapping("/{id}")
  @RequirePermission("USER_READ")
  public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
    try {
      UserDTO user = userService.findById(id);
      return ResponseEntity.ok(ApiResponse.success(user));
    } catch (Exception e) {
      log.error("Get user failed", e);
      return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
    }
  }

  @GetMapping
  @RequirePermission("USER_READ")
  public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
    try {
      List<UserDTO> users = userService.findAll();
      return ResponseEntity.ok(ApiResponse.success(users));
    } catch (Exception e) {
      log.error("Get all users failed", e);
      return ResponseEntity.badRequest().body(ApiResponse.error(500, "Internal server error"));
    }
  }

  @PutMapping("/{userId}")
  @RequirePermission("USER_WRITE")
  public ResponseEntity<ApiResponse<UserDTO>> updateUserWithPermissions(
      @PathVariable Long userId, @RequestBody UpdateUserRequest request) {
    try {
      UserDTO updatedUser = userService.updateUser(userId, request);
      return ResponseEntity.ok(ApiResponse.success(updatedUser));
    } catch (Exception e) {
      log.error("Update user failed", e);
      return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  @RequirePermission("USER_DELETE")
  public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
    try {
      log.info("Deleting user with id: {}", id);
      userService.deleteById(id);
      return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    } catch (RuntimeException e) {
      log.error("Delete user failed: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
    }
  }
}
