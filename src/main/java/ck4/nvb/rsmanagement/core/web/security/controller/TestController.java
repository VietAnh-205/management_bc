package ck4.nvb.rsmanagement.core.web.security.controller;

import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

  /** public endpoint - no authentication required */
  @GetMapping("/public")
  public ResponseEntity<Map<String, Object>> publicEndpoint() {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "This is a public endpoint");
    response.put("timestamp", System.currentTimeMillis());
    return ResponseEntity.ok(response);
  }

  /** protected endpoint - requires authentication */
  @GetMapping("/protected")
  public ResponseEntity<Map<String, Object>> protectedEndpoint() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserRoleDto userRole = (UserRoleDto) authentication.getPrincipal();

    Map<String, Object> response = new HashMap<>();
    response.put("message", "This is a protected endpoint");
    response.put("user", userRole.getUserName());
    response.put("userId", userRole.getUserId());
    response.put("storeId", userRole.getStoreId());
    response.put("roleId", userRole.getRoleId());
    response.put("permissions", userRole.getPermissions());
    response.put("timestamp", System.currentTimeMillis());

    log.info("User {} accessed protected endpoint", userRole.getUserName());

    return ResponseEntity.ok(response);
  }

  /** admin only endpoint - requires USER_WRITE permission */
  @GetMapping("/admin")
  @PreAuthorize("hasAuthority('SYSTEM_CONFIG')")
  public ResponseEntity<Map<String, Object>> adminEndpoint() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserRoleDto userRole = (UserRoleDto) authentication.getPrincipal();

    Map<String, Object> response = new HashMap<>();
    response.put("message", "This is an admin-only endpoint");
    response.put("user", userRole.getUserName());
    response.put("permissions", userRole.getPermissions());
    response.put("timestamp", System.currentTimeMillis());

    log.info("Admin user {} accessed admin endpoint", userRole.getUserName());

    return ResponseEntity.ok(response);
  }
}
