package ck4.nvb.rsmanagement.config;

import ck4.nvb.rsmanagement.core.module.users.permission.domain.entity.Permission;
import ck4.nvb.rsmanagement.core.module.users.permission.domain.repository.PermissionRepository;
import ck4.nvb.rsmanagement.core.module.users.role.domain.entity.Role;
import ck4.nvb.rsmanagement.core.module.users.role.domain.repository.RoleRepository;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.domain.entity.RolePermission;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.domain.repository.RolePermissionRepository;
import ck4.nvb.rsmanagement.core.module.users.user.domain.User;
import ck4.nvb.rsmanagement.core.module.users.user.domain.UserRepository;
import ck4.nvb.rsmanagement.core.module.users.userrole.domain.entity.UserRole;
import ck4.nvb.rsmanagement.core.module.users.userrole.domain.repository.UserRoleRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final UserRoleRepository userRoleRepository;
  private final RolePermissionRepository rolePermissionRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    if (userRepository.count() == 0) {
      // Create permissions
      Permission userRead = createPermission("USER_READ", "Read user information");
      Permission userWrite = createPermission("USER_WRITE", "Create and update users");
      Permission userDelete = createPermission("USER_DELETE", "Delete users");

      Permission roleRead = createPermission("ROLE_READ", "Read roles");
      Permission roleWrite = createPermission("ROLE_WRITE", "Create and update roles");

      Permission fullRole = createPermission("FULL_ROLE", "Full admin access");

      Permission productRoleRead = createPermission("PRODUCT_READ", "Read product roles");
      Permission productRoleWrite =
          createPermission("PRODUCT_WRITE", "Create and update product roles");
      Permission productRoleDelete = createPermission("PRODUCT_DELETE", "Delete product roles");
      Permission proudctFullRole = createPermission("PRODUCT_FULL_ROLE", "Full admin access");

      Permission batchRole = createPermission("BATCH_ROLE", "Read batch roles");

      Permission categoryRole = createPermission("CATEGORY_ROLE", "Read category roles");

      Permission storeRole = createPermission("STORE_ROLE", "Read store roles");

      Permission supplierRole = createPermission("SUPPLIER_ROLE", "Read supplier roles");

      Permission transferRole = createPermission("TRANSFER_ROLE", "Read transfer roles");

      Permission batchStockRole = createPermission("BATCH_STOCK_ROLE", "Read batch stock roles");

      Permission customerRole = createPermission("CUSTOMER_ROLE", "Read customer roles");

      Permission paymentRole = createPermission("PAYMENT_ROLE", "Read payment roles");

      Permission orderRoleRead = createPermission("ORDER_READ", "Read order roles");
      Permission orderRoleWrite = createPermission("ORDER_WRITE", "Write order roles");
      Permission orderRoleDelete = createPermission("ORDER_DELETE", "Delete order roles");
      Permission orderFullRole = createPermission("ORDER_FULL_ROLE", "Read order roles");

      Permission voucherRole = createPermission("VOUCHER_ROLE", "Read voucher roles");

      // Create roles
      Role adminRole = createRole("ADMIN", "System Administrator");
      Role managerRole = createRole("MANAGER", "Store Manager");
      Role employeeRole = createRole("EMPLOYEE", "Store Employee");

      // Assign permissions to roles
      assignPermissionsToRole(adminRole, Arrays.asList(fullRole));
      assignPermissionsToRole(
          managerRole,
          Arrays.asList(
              userRead, userWrite, roleRead, roleWrite, storeRole, proudctFullRole, orderFullRole));
      assignPermissionsToRole(
          employeeRole,
          Arrays.asList(userRead, roleRead, productRoleRead, orderRoleRead, orderRoleWrite));

      // Create users
      User admin =
          createUser(
              "vietanh",
              "password123",
              "System Admin",
              "admin@circlek.com",
              "0123456789",
              1967119892788416522L);
      User manager =
          createUser(
              "vietand",
              "password123",
              "Store Manager",
              "manager1@circlek.com",
              "0123456788",
              1967119892788416523L);
      User employee =
          createUser(
              "vietend",
              "password123",
              "Store Employee",
              "employee1@circlek.com",
              "0123456787",
              1967119892788416524L);

      // Assign roles to users
      assignRoleToUser(admin, adminRole);
      assignRoleToUser(manager, managerRole);
      assignRoleToUser(employee, employeeRole);

      log.info("Sample data initialized successfully!");
    }
  }

  private Permission createPermission(String code, String description) {
    Permission permission = Permission.builder().code(code).description(description).build();
    return permissionRepository.save(permission);
  }

  private Role createRole(String name, String description) {
    Role role =
        Role.builder()
            .name(name)
            .description(description)
            .createdTime(LocalDateTime.now())
            .creatorId(1L)
            .deleted(false)
            .build();
    return roleRepository.save(role);
  }

  private User createUser(
      String userName, String password, String fullName, String email, String phone, Long storeId) {
    User user =
        User.builder()
            .username(userName)
            .password(passwordEncoder.encode(password))
            .name(fullName)
            .email(email)
            .phone(phone)
            .storeId(storeId)
            .createdTime(LocalDateTime.now())
            .creatorId(1L)
            .build();
    return userRepository.save(user);
  }

  private void assignPermissionsToRole(Role role, java.util.List<Permission> permissions) {
    permissions.forEach(
        permission -> {
          RolePermission rolePermission =
              RolePermission.builder()
                  .roleId(role.getId())
                  .permissionId(permission.getId())
                  .build();
          rolePermissionRepository.save(rolePermission);
        });
  }

  private void assignRoleToUser(User user, Role role) {
    UserRole userRole =
        UserRole.builder().userId(user.getId()).roleId(role.getId()).user(user).role(role).build();
    userRoleRepository.save(userRole);
  }
}
