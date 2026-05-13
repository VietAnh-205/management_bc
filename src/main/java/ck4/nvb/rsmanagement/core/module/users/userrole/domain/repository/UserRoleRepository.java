package ck4.nvb.rsmanagement.core.module.users.userrole.domain.repository;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import ck4.nvb.rsmanagement.core.module.users.role.service.dto.UserRoleProjection;
import ck4.nvb.rsmanagement.core.module.users.userrole.domain.entity.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("userRoleRepository")
public interface UserRoleRepository extends BaseFullAuditedRepository<UserRole, Long, Long> {
  List<UserRole> findByUserId(Long userId);

  List<UserRole> findByStoreId(Long storeId);

  List<UserRole> findByRoleId(Long roleId);

  void deleteByUserId(Long userId);

  void deleteByUserIdAndRoleId(Long userId, Long roleId);

  @Query(
      "SELECT ur FROM UserRole ur LEFT JOIN FETCH ur.role r LEFT JOIN FETCH r.rolePermissions rp LEFT JOIN FETCH rp.permission p WHERE ur.userId = :userId")
  List<UserRole> findByUserIdWithPermissions(@Param("userId") Long userId);

  void deleteByUserIdAndRoleIdAndStoreId(Long userId, Long roleId, Long storeId);

  @Query(
      value =
          """
    SELECT
      ur.user_id AS userId,
      ur.role_id AS roleId,
      ur.store_id AS storeId,
      u.username AS userName,
      u.full_name AS fullName,
      u.email AS email,
      u.phone AS phone,
      r.name AS roleName,
      STRING_AGG(p.code, ',') AS permissions
    FROM user_role ur
    JOIN store_user u ON ur.user_id = u.id
    JOIN role r ON ur.role_id = r.id
    LEFT JOIN role_permission rp ON r.id = rp.role_id
    LEFT JOIN permission p ON rp.permission_id = p.id
    WHERE ur.user_id = :userId AND ur.role_id = :roleId
    GROUP BY ur.user_id, ur.role_id, ur.store_id, u.username, u.full_name, u.email, u.phone, r.name
    """,
      nativeQuery = true)
  UserRoleProjection findInfoByUserIdAndRoleId(Long userId, Long roleId);
}
