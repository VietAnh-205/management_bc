package ck4.nvb.rsmanagement.core.module.users.role.service.dto;

public interface UserRoleProjection {
  Long getUserId();

  Long getRoleId();

  Long getStoreId();

  String getUserName();

  String getFullName();

  String getEmail();

  String getPhone();

  String getRoleName();

  String getPermissions();
}
