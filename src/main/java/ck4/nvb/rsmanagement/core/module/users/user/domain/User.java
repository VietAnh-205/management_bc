package ck4.nvb.rsmanagement.core.module.users.user.domain;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import ck4.nvb.rsmanagement.core.module.users.userrole.domain.entity.UserRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "store_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends FullAuditedSerialIdEntity implements UserDetails {

  @Column(name = "username", nullable = false, unique = true)
  private String username;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "full_name")
  private String name;

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "phone")
  private String phone;

  @Column(name = "store_id")
  private Long storeId;

  @Column(name = "last_login")
  LocalDateTime lastLogin;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  List<UserRole> userRoles;

  public User(Long id) {
    setId(id);
  }

  // UserDetails implementation
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("USER"));
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }
}
