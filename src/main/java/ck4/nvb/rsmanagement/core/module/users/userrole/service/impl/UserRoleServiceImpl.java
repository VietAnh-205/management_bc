package ck4.nvb.rsmanagement.core.module.users.userrole.service.impl;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.users.role.service.dto.UserRoleProjection;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import ck4.nvb.rsmanagement.core.module.users.userrole.domain.entity.UserRole;
import ck4.nvb.rsmanagement.core.module.users.userrole.domain.repository.UserRoleRepository;
import ck4.nvb.rsmanagement.core.module.users.userrole.service.dto.UserRoleDto;
import java.util.List;
import java.util.Map;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("userRoleService")
public class UserRoleServiceImpl
    extends FullAuditedCrudServiceImpl<UserRoleDto, UserRole, Long, UserGetDto, Long>
    implements FullAuditedCrudService<UserRoleDto, UserRole, Long, UserGetDto, Long> {

  private final ModelMapper modelMapper = new ModelMapper();

  protected UserRoleServiceImpl(UserRoleRepository repository) {
    super(repository, UserRole.class);
  }

  @Override
  public UserRoleRepository getRepository() {
    return (UserRoleRepository) super.getRepository();
  }

  @Override
  public UserRoleDto mapToEntityDto(UserRole entity) {
    return modelMapper.map(entity, UserRoleDto.class);
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("user_id", List.of(SearchOperator.EQUALS));
    keys.put("store_id", List.of(SearchOperator.EQUALS));
    return keys;
  }

  public UserRoleDto getFullInfoByUserIdAndRoleId(long userId, long roleId) {
    UserRoleProjection projection = getRepository().findInfoByUserIdAndRoleId(userId, roleId);
    UserRoleDto response = new UserRoleDto();
    response.setUserId(projection.getUserId());
    response.setRoleId(projection.getRoleId());
    response.setStoreId(projection.getStoreId());
    response.setUserName(projection.getUserName());
    response.setFullName(projection.getFullName());
    response.setEmail(projection.getEmail());
    response.setPhone(projection.getPhone());
    response.setRoleName(projection.getRoleName());

    if (projection.getPermissions() != null && !projection.getPermissions().isEmpty()) {
      response.setPermissions(List.of(projection.getPermissions().split(",")));
    } else {
      response.setPermissions(List.of());
    }

    return response;
  }
}
