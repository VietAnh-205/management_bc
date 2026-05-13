package ck4.nvb.rsmanagement.core.module.users.rolepermission.domain.entity;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import ck4.nvb.rsmanagement.core.module.users.permission.domain.entity.Permission;
import ck4.nvb.rsmanagement.core.module.users.role.domain.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@Table(name = "role_permission")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RolePermission extends FullAuditedSerialIdEntity {

  @Column(name = "role_id")
  private Long roleId;

  @Column(name = "permission_id")
  private Long permissionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", insertable = false, updatable = false)
  Role role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "permission_id", insertable = false, updatable = false)
  Permission permission;
}
