package ck4.nvb.rsmanagement.core.module.users.role.domain.repository;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import ck4.nvb.rsmanagement.core.module.users.role.domain.entity.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("roleRepository")
public interface RoleRepository extends BaseFullAuditedRepository<Role, Long, Long> {
  Optional<Role> findByName(String name);

  List<Role> findByDeletedFalse();

  @Query(
      "SELECT r FROM Role r LEFT JOIN FETCH r.rolePermissions rp LEFT JOIN FETCH rp.permission p WHERE r.id = :roleId AND r.deleted = false")
  Optional<Role> findByIdWithPermissions(@Param("roleId") Long roleId);
}
