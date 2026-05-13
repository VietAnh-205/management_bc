package ck4.nvb.rsmanagement.core.module.users.user.service;

import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import java.util.List;

public interface UserGetServiceWithRole<T, D> {

  UserRoleDto getByUsernameAndPassword(String username, String password) throws AppException;

  UserRoleDto getByUsernameAndPasswordAndStore(String username, String password, Long storeId)
      throws AppException;

  UserRoleDto get(Long userId) throws AppException;

  UserRoleDto getByUsername(String username) throws AppException;

  List<UserRoleDto> getAllUserRoles(Long userId) throws AppException;

  UserRoleDto getUserSession(Long userId, Long storeId) throws AppException;
}
