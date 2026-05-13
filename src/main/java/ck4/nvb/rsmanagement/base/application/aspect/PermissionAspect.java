package ck4.nvb.rsmanagement.base.application.aspect;

import ck4.nvb.rsmanagement.base.application.annotation.RequirePermission;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import ck4.nvb.rsmanagement.core.web.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {

  private final AuthService authService;

  @Around("@annotation(requirePermission)")
  public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission)
      throws Throwable {
    Long currentUserId = getCurrentUserId();

    if (currentUserId == null) {
      throw new RuntimeException("User not authenticated");
    }

    String[] requiredPermissions = requirePermission.value();
    RequirePermission.LogicType logic = requirePermission.logic();

    boolean hasAccess = checkPermissions(currentUserId, requiredPermissions, logic);

    if (!hasAccess) {
      String permissionStr =
          String.join(
              logic == RequirePermission.LogicType.ALL ? " AND " : " OR ", requiredPermissions);
      throw new RuntimeException("Access denied. Required permission: " + permissionStr);
    }

    return joinPoint.proceed();
  }

  private boolean checkPermissions(
      Long userId, String[] permissions, RequirePermission.LogicType logic) {
    if (logic == RequirePermission.LogicType.ALL) {
      // ALL logic: User phải có tất cả permissions
      for (String permission : permissions) {
        if (!authService.hasPermission(userId, permission)) {
          return false;
        }
      }
      return true;
    } else {
      // ANY logic: User chỉ cần có ít nhất 1 permission
      for (String permission : permissions) {
        if (authService.hasPermission(userId, permission)) {
          return true;
        }
      }
      return false;
    }
  }

  private Long getCurrentUserId() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      Object principal = authentication.getPrincipal();

      // Case 1: Principal là UserGetDto
      if (principal instanceof UserGetDto) {
        UserGetDto user = (UserGetDto) principal;
        return user.getId();
      }

      // Case 2: Principal là UserRoleDto
      if (principal instanceof UserRoleDto) {
        UserRoleDto userRole = (UserRoleDto) principal;
        return userRole.getUserId();
      }

      // Case 3: Principal là String (username)
      if (principal instanceof String) {
        String username = (String) principal;
        return authService.getUserIdByUsername(username);
      }
    }
    return null;
  }
}
