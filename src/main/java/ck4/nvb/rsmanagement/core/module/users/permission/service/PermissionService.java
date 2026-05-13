package ck4.nvb.rsmanagement.core.module.users.permission.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.users.permission.domain.entity.Permission;
import ck4.nvb.rsmanagement.core.module.users.permission.service.dto.PermissionDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

public interface PermissionService
    extends FullAuditedCrudService<PermissionDto, Permission, Long, UserGetDto, Long> {}
