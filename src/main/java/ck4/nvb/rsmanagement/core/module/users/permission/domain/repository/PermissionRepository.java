package ck4.nvb.rsmanagement.core.module.users.permission.domain.repository;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import ck4.nvb.rsmanagement.core.module.users.permission.domain.entity.Permission;
import org.springframework.stereotype.Repository;

@Repository("permissionRepository")
public interface PermissionRepository extends BaseFullAuditedRepository<Permission, Long, Long> {
  Permission findByCode(String code);
}
