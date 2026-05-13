package ck4.nvb.rsmanagement.core.web.security.service;

import ck4.nvb.rsmanagement.core.module.users.user.domain.User;
import ck4.nvb.rsmanagement.core.module.users.user.domain.UserRepository;
import ck4.nvb.rsmanagement.core.module.users.userrole.domain.entity.UserRole;
import ck4.nvb.rsmanagement.core.module.users.userrole.domain.repository.UserRoleRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final UserRoleRepository userRoleRepository;
  private final UserRepository userRepository;

  public List<String> getUserRoles(Long userId) {
    List<UserRole> userRoles = userRoleRepository.findByUserIdWithPermissions(userId);
    return userRoles.stream()
        .map(
            ur -> {
              if (ur.getRole() == null) {
                return null;
              }
              return ur.getRole().getName();
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public List<String> getUserPermissions(Long userId) {
    List<UserRole> userRoles = userRoleRepository.findByUserIdWithPermissions(userId);

    return userRoles.stream()
        .filter(ur -> ur.getRole() != null)
        .flatMap(
            ur -> {
              if (ur.getRole().getRolePermissions() == null) {
                return Stream.empty();
              }
              return ur.getRole().getRolePermissions().stream();
            })
        .filter(rp -> rp.getPermission() != null)
        .map(rp -> rp.getPermission().getCode())
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());
  }

  public boolean hasPermission(Long userId, String permissionCode) {
    List<String> permissions = getUserPermissions(userId);
    return permissions.contains(permissionCode);
  }

  public boolean hasRole(Long userId, String roleName) {
    List<String> roles = getUserRoles(userId);
    return roles.contains(roleName);
  }

  public Long getUserIdByUsername(String username) {
    return userRepository.findByUsername(username).map(User::getId).orElse(null);
  }
}
