package ck4.nvb.rsmanagement.core.module.users.userrole.domain.entity;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import ck4.nvb.rsmanagement.core.module.users.role.domain.entity.Role;
import ck4.nvb.rsmanagement.core.module.users.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@Table(name = "user_role")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole extends FullAuditedSerialIdEntity {

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "role_id")
  private Long roleId;

  @Column(name = "store_id")
  private Long storeId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", insertable = false, updatable = false)
  Role role;
}
