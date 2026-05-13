package ck4.nvb.rsmanagement.core.module.users.user.service.impl;

import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.core.module.users.permission.domain.entity.Permission;
import ck4.nvb.rsmanagement.core.module.users.permission.domain.repository.PermissionRepository;
import ck4.nvb.rsmanagement.core.module.users.role.domain.entity.Role;
import ck4.nvb.rsmanagement.core.module.users.role.domain.repository.RoleRepository;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.domain.entity.RolePermission;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.domain.repository.RolePermissionRepository;
import ck4.nvb.rsmanagement.core.module.users.user.domain.User;
import ck4.nvb.rsmanagement.core.module.users.user.domain.UserRepository;
import ck4.nvb.rsmanagement.core.module.users.user.service.UserGetServiceWithRole;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.*;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.request.CreateUserRequest;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.request.LoginRequest;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.request.UpdateUserRequest;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.response.LoginResponse;
import ck4.nvb.rsmanagement.core.module.users.userrole.domain.entity.UserRole;
import ck4.nvb.rsmanagement.core.module.users.userrole.domain.repository.UserRoleRepository;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import ck4.nvb.rsmanagement.core.web.security.service.AuthService;
import ck4.nvb.rsmanagement.core.web.util.CommonPasswordEncoder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userGetServiceWithRole")
@RequiredArgsConstructor
@Slf4j
public class UserGetServiceWithRoleImpl<D, ID, T> implements UserGetServiceWithRole {

  @Value("${jwt.secret}")
  private String JWT_SECRET;

  @Value("${jwt.expiration}")
  private long JWT_EXPIRATION;

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final RoleRepository roleRepository;
  private final RolePermissionRepository rolePermissionRepository;
  private final PermissionRepository permissionRepository;
  private final CommonPasswordEncoder passwordEncoder;
  private final AuthService authService;

  @Override
  public UserRoleDto getByUsernameAndPassword(String username, String password)
      throws AppException {
    User user = authenticateUser(username, password);
    return getPrimaryUserRole(user);
  }

  @Override
  public UserRoleDto getByUsernameAndPasswordAndStore(
      String username, String password, Long storeId) throws AppException {
    User user = authenticateUser(username, password);
    return getUserRoleForStore(user, storeId);
  }

  @Override
  public UserRoleDto get(Long userId) throws AppException {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new AppException("User not found"));
    return getPrimaryUserRole(user);
  }

  @Override
  public UserRoleDto getByUsername(String username) throws AppException {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new AppException("User not found"));
    return getPrimaryUserRole(user);
  }

  @Override
  public List<UserRoleDto> getAllUserRoles(Long userId) throws AppException {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new AppException("User not found"));

    List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
    return userRoles.stream().map(this::buildUserRoleDto).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public UserRoleDto getUserSession(Long userId, Long storeId) throws AppException {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new AppException("User not found"));

    List<UserRoleDto> allRoles = getAllUserRoles(userId);
    UserRoleDto currentRole =
        allRoles.stream()
            .filter(role -> role.getStoreId().equals(storeId))
            .findFirst()
            .orElseThrow(() -> new AppException("User has no role for this store"));
    return currentRole;
  }

  private User authenticateUser(String username, String password) throws AppException {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new AppException("User not found"));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new AppException("Invalid password");
    }

    if (user.isDeleted()) {
      throw new AppException("User is disabled");
    }

    return user;
  }

  private UserRoleDto getPrimaryUserRole(User user) throws AppException {
    List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
    if (userRoles.isEmpty()) {
      throw new AppException("User has no roles assigned");
    }

    // get first role (primary role)
    UserRole userRole = userRoles.get(0);
    return buildUserRoleDto(userRole, user);
  }

  private UserRoleDto getUserRoleForStore(User user, Long storeId) throws AppException {
    List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
    UserRole userRole =
        userRoles.stream()
            .filter(ur -> ur.getStoreId().equals(storeId))
            .findFirst()
            .orElseThrow(() -> new AppException("User has no role for this store"));

    return buildUserRoleDto(userRole, user);
  }

  private UserRoleDto buildUserRoleDto(UserRole userRole, User user) throws AppException {
    // get role information
    Role role =
        roleRepository
            .findById(userRole.getRoleId())
            .orElseThrow(() -> new AppException("Role not found"));

    // get permissions for role
    List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(role.getId());
    List<String> permissions =
        rolePermissions.stream()
            .map(
                rp -> {
                  Permission permission =
                      permissionRepository.findById(rp.getPermissionId()).orElse(null);
                  return permission != null ? permission.getCode() : null;
                })
            .filter(code -> code != null)
            .collect(Collectors.toList());

    UserRoleDto userRoleDto = new UserRoleDto();
    userRoleDto.setId(userRole.getId());
    userRoleDto.setUserId(user.getId());
    userRoleDto.setRoleId(role.getId());
    userRoleDto.setStoreId(userRole.getStoreId());

    // user information
    userRoleDto.setUserName(user.getUsername());
    userRoleDto.setFullName(user.getName());
    userRoleDto.setEmail(user.getEmail());
    userRoleDto.setPhone(user.getPhone());

    // role information
    userRoleDto.setRoleName(role.getName());

    // permissions
    userRoleDto.setPermissions(permissions);

    return userRoleDto;
  }

  private UserRoleDto buildUserRoleDto(UserRole userRole) throws AppException {
    User user =
        userRepository
            .findById(userRole.getUserId())
            .orElseThrow(() -> new AppException("User not found"));
    return buildUserRoleDto(userRole, user);
  }

  /*

  login

   */
  public LoginResponse login(LoginRequest request) {
    Optional<User> userOpt = userRepository.findByUserNameWithRoles(request.getUserName());

    if (userOpt.isEmpty()) {
      throw new RuntimeException("User not found");
    }

    User user = userOpt.get();

    if (!passwordEncoder.matches(request.getPassWord(), user.getPassword())) {
      throw new RuntimeException("Invalid password");
    }

    user.setLastLogin(LocalDateTime.now());
    userRepository.save(user);

    List<String> roles = authService.getUserRoles(user.getId());
    List<String> permissions = authService.getUserPermissions(user.getId());

    String token =
        generateJwtToken(user.getId(), user.getUsername(), roles, permissions, user.getStoreId());

    return LoginResponse.builder()
        .id(user.getId())
        .userName(user.getUsername())
        .fullName(user.getName())
        .email(user.getEmail())
        .phone(user.getPhone())
        .storeId(user.getStoreId())
        .lastLogin(user.getLastLogin())
        .token(token)
        .roles(roles)
        .permissions(permissions)
        .build();
  }

  private String generateJwtToken(
      Long userId, String username, List<String> roles, List<String> permissions, Long storeId) {

    if (roles == null) roles = new ArrayList<>();
    if (permissions == null) permissions = new ArrayList<>();

    // Nếu JWT_SECRET là Base64 encoded:
    byte[] keyBytes = Base64.getDecoder().decode(JWT_SECRET);
    Key key = Keys.hmacShaKeyFor(keyBytes);

    return Jwts.builder()
        .setSubject(username)
        .claim("userId", String.valueOf(userId))
        .claim("storeId", String.valueOf(storeId))
        .claim("roles", roles)
        .claim("permissions", permissions)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  // created
  @Transactional
  public UserDTO createUser(CreateUserRequest request) {
    if (userRepository.existsByUsername(request.getUserName())) {
      log.error("Username already exists: {}", request.getUserName());
      throw new RuntimeException("Username already exists");
    }

    if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
      log.error("Email already exists: {}", request.getEmail());
      throw new RuntimeException("Email already exists");
    }

    Long currentUserId = getCurrentUserId();

    User user =
        User.builder()
            .username(request.getUserName())
            .password(passwordEncoder.encode(request.getPassWord()))
            .name(request.getFullName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .storeId(request.getStoreId())
            .createdTime(LocalDateTime.now())
            .creatorId(currentUserId)
            .build();

    User savedUser = userRepository.save(user);

    Set<Long> roleIds = request.getRoleIds();
    if (roleIds != null && !roleIds.isEmpty()) {
      List<UserRole> userRoles =
          roleIds.stream()
              .map(
                  roleId -> {
                    if (!roleRepository.existsById(roleId)) {
                      log.warn("Role ID not found: {}", roleId);
                      return null;
                    }
                    Role role =
                        roleRepository
                            .findById(roleId)
                            .orElseThrow(
                                () -> {
                                  log.error("Role not found: {}", roleId);
                                  return new RuntimeException("Role not found: " + roleId);
                                });
                    return UserRole.builder()
                        .userId(savedUser.getId())
                        .roleId(roleId)
                        .user(savedUser)
                        .role(role)
                        .build();
                  })
              .filter(Objects::nonNull)
              .collect(Collectors.toList());

      if (!userRoles.isEmpty()) {
        userRoleRepository.saveAll(userRoles);
        log.info("Assigned {} roles to user: {}", userRoles.size(), savedUser.getUsername());
      } else {
        log.warn("No valid role IDs provided for user: {}", savedUser.getUsername());
      }
    } else {
      log.warn("No role IDs provided for user: {}", savedUser.getUsername());
    }

    List<String> roles = authService.getUserRoles(savedUser.getId());
    List<String> permissions = rolePermissionRepository.findPermissionCodesByRoleIds(roleIds);

    log.info("User created with roles: {}, permissions: {}", roles, permissions);

    return UserDTO.builder()
        .id(savedUser.getId())
        .userName(savedUser.getUsername())
        .fullName(savedUser.getName())
        .email(savedUser.getEmail())
        .phone(savedUser.getPhone())
        .storeId(savedUser.getStoreId())
        .lastLogin(savedUser.getLastLogin())
        .createdTime(savedUser.getCreatedTime())
        .creatorId(savedUser.getCreatorId())
        .updatedTime(savedUser.getUpdatedTime())
        .updaterId(savedUser.getUpdaterID())
        .roles(roles)
        .permissions(permissions)
        .build();
  }

  /*
  update User
   */

  @Transactional
  public UserDTO updateUser(Long userId, UpdateUserRequest request) {
    log.info("Updating user with id: {}", userId);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.error("User not found: {}", userId);
                  return new RuntimeException("User not found: " + userId);
                });

    Long currentUserId = getCurrentUserId();

    if (request.getFullName() != null) user.setName(request.getFullName());
    if (request.getEmail() != null) user.setEmail(request.getEmail());
    if (request.getPhone() != null) user.setPhone(request.getPhone());
    if (request.getStoreId() != null) user.setStoreId(request.getStoreId());
    if (request.getPassWord() != null) {
      user.setPassword(passwordEncoder.encode(request.getPassWord()));
    }

    user.setUpdatedTime(LocalDateTime.now());
    user.setUpdaterID(currentUserId);

    User updatedUser = userRepository.save(user);
    log.info("Updated user: id={}, username={}", updatedUser.getId(), updatedUser.getUsername());

    List<String> roles = authService.getUserRoles(updatedUser.getId());
    List<String> permissions =
        rolePermissionRepository.findPermissionCodesByRoleIds(
            userRoleRepository.findByUserId(userId).stream()
                .map(ur -> ur.getRole().getId())
                .collect(Collectors.toSet()));

    log.info("User updated with roles: {}, permissions: {}", roles, permissions);

    return UserDTO.builder()
        .id(updatedUser.getId())
        .userName(updatedUser.getUsername())
        .fullName(updatedUser.getName())
        .email(updatedUser.getEmail())
        .phone(updatedUser.getPhone())
        .storeId(updatedUser.getStoreId())
        .lastLogin(updatedUser.getLastLogin())
        .createdTime(updatedUser.getCreatedTime())
        .creatorId(updatedUser.getCreatorId())
        .updatedTime(updatedUser.getUpdatedTime())
        .updaterId(updatedUser.getUpdaterID())
        .roles(roles)
        .permissions(permissions)
        .build();
  }

  public Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof String) {
      String username = (String) authentication.getPrincipal();
      return authService.getUserIdByUsername(username);
    }
    return null;
  }

  public UserDTO findById(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.error("User not found: {}", userId);
                  return new RuntimeException("User not found");
                });

    return convertToDTO(user);
  }

  public List<UserDTO> findAll() {
    log.info("Fetching all users");
    List<User> users = userRepository.findAll();
    return users.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  @Transactional
  public void deleteById(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.error("User not found: {}", userId);
                  return new RuntimeException("User not found: " + userId);
                });
    userRoleRepository.deleteByUserId(userId);
    userRepository.delete(user);
    log.info("Deleted user with id: {}", userId);
  }

  protected UserDTO convertToDTO(User entity) {
    List<String> roles = authService.getUserRoles(entity.getId());
    List<String> permissions =
        rolePermissionRepository.findPermissionCodesByRoleIds(
            userRoleRepository.findByUserId(entity.getId()).stream()
                .map(ur -> ur.getRole().getId())
                .collect(Collectors.toSet()));

    return UserDTO.builder()
        .id(entity.getId())
        .userName(entity.getUsername())
        .fullName(entity.getName())
        .email(entity.getEmail())
        .phone(entity.getPhone())
        .storeId(entity.getStoreId())
        .lastLogin(entity.getLastLogin())
        .createdTime(entity.getCreatedTime())
        .creatorId(entity.getCreatorId())
        .updatedTime(entity.getUpdatedTime())
        .updaterId(entity.getUpdaterID())
        .roles(roles)
        .permissions(permissions)
        .build();
  }

  protected User convertToEntity(UserDTO dto) {
    return User.builder()
        .username(dto.getUserName())
        .name(dto.getFullName())
        .email(dto.getEmail())
        .phone(dto.getPhone())
        .storeId(dto.getStoreId())
        .lastLogin(dto.getLastLogin())
        .build();
  }

  void updateEntityFromDTO(User entity, UserDTO dto) {
    if (dto.getFullName() != null) entity.setName(dto.getFullName());
    if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
    if (dto.getPhone() != null) entity.setPhone(dto.getPhone());
    if (dto.getStoreId() != null) entity.setStoreId(dto.getStoreId());

    Long currentUserId = getCurrentUserId();
    entity.setUpdatedTime(LocalDateTime.now());
    entity.setUpdaterID(currentUserId);
  }
}
