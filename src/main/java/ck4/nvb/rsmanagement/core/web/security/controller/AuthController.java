package ck4.nvb.rsmanagement.core.web.security.controller;

import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.core.module.users.user.domain.User;
import ck4.nvb.rsmanagement.core.module.users.user.domain.UserRepository;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserDTO;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.request.LoginRequest;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.response.LoginResponse;
import ck4.nvb.rsmanagement.core.module.users.user.service.impl.UserGetServiceWithRoleImpl;
import ck4.nvb.rsmanagement.core.web.security.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
@RestController
@RequestMapping("/${rs.api.main.publicUrl}/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final UserGetServiceWithRoleImpl userService;
  private final AuthService authService;
  private final UserRepository userRepository;

  @Value("${jwt.secret}")
  private String jwtSecret;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
    try {
      LoginResponse response = userService.login(request);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception e) {
      log.error("Login failed", e);
      return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<String>> logout() {
    return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(HttpServletRequest request) {
    try {
      String token = extractToken(request);
      if (token == null) {
        return ResponseEntity.status(401).body(ApiResponse.error(401, "No token provided"));
      }

      Claims claims = getClaims(token);
      String username = claims.getSubject();

      User user =
          userRepository
              .findByUserNameWithRoles(username)
              .orElseThrow(() -> new ChangeSetPersister.NotFoundException());

      // Get roles and permissions
      List<String> roles = authService.getUserRoles(user.getId());
      List<String> permissions = authService.getUserPermissions(user.getId());

      UserDTO userDTO =
          UserDTO.builder()
              .userName(user.getUsername())
              .fullName(user.getName())
              .email(user.getEmail())
              .phone(user.getPhone())
              .storeId(user.getStoreId())
              .lastLogin(user.getLastLogin())
              .roles(roles)
              .permissions(permissions)
              .build();
      userDTO.setId(user.getId());
      userDTO.setCreatedTime(user.getCreatedTime());
      userDTO.setCreatorId(user.getCreatorId());
      userDTO.setUpdatedTime(user.getUpdatedTime());
      userDTO.setUpdaterId(user.getUpdaterID());

      return ResponseEntity.ok(ApiResponse.success(userDTO));
    } catch (Exception e) {
      log.error("Get current user failed", e);
      return ResponseEntity.status(401).body(ApiResponse.error(401, "Invalid token"));
    }
  }

  // Helper methods - thêm vào AuthController
  private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  private Claims getClaims(String token) {
    byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
    SecretKey key = Keys.hmacShaKeyFor(keyBytes);

    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
