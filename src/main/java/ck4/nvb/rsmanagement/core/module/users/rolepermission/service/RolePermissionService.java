package ck4.nvb.rsmanagement.core.module.users.rolepermission.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.domain.entity.RolePermission;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.service.dto.RolePermissionDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

public interface RolePermissionService
    extends FullAuditedCrudService<RolePermissionDto, RolePermission, Long, UserGetDto, Long> {}
