package ck4.nvb.rsmanagement.core.module.users.role.domain.entity;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.domain.entity.RolePermission;
import ck4.nvb.rsmanagement.core.module.users.userrole.domain.entity.UserRole;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@Table(name = "role")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role extends FullAuditedSerialIdEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  List<UserRole> userRoles;

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  List<RolePermission> rolePermissions;
}
