package ck4.nvb.rsmanagement.core.module.users.user.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("UserRepository")
public interface UserRepository extends BaseFullAuditedRepository<User, Long, Long> {
  Optional<User> findByUsername(String userName);

  @Query("select u from User u where u.storeId = :storeId and u.deleted = false")
  Page<User> findByStoreId(@Param("storeId") Long storeId, Pageable pageable);

  boolean existsByUsername(String userName);

  boolean existsByEmail(String email);

  @Query(
      "SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role r WHERE u.username = :userName")
  Optional<User> findByUserNameWithRoles(@Param("userName") String userName);
}
