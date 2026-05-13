package ck4.nvb.rsmanagement.core.module.users.rolepermission.domain.repository;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.domain.entity.RolePermission;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("rolePermissionRepository")
public interface RolePermissionRepository
    extends BaseFullAuditedRepository<RolePermission, Long, Long> {
  List<RolePermission> findByRoleId(Long roleId);

  List<RolePermission> findByPermissionId(Long permissionId);

  List<RolePermission> findByRoleIdIn(Set<Long> roleIds);

  boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

  void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);

  @Query(
      "SELECT p.code FROM Permission p JOIN RolePermission rp ON p.id = rp.permission.id "
          + "WHERE rp.role.id IN :roleIds")
  List<String> findPermissionCodesByRoleIds(@Param("roleIds") Set<Long> roleIds);
}
