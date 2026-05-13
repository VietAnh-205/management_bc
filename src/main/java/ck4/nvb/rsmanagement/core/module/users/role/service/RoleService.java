package ck4.nvb.rsmanagement.core.module.users.role.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.users.role.domain.entity.Role;
import ck4.nvb.rsmanagement.core.module.users.role.service.dto.RoleDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

public interface RoleService
    extends FullAuditedCrudService<RoleDto, Role, Long, UserGetDto, Long> {}
