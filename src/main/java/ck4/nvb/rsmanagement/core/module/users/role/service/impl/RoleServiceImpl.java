package ck4.nvb.rsmanagement.core.module.users.role.service.impl;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.users.role.domain.entity.Role;
import ck4.nvb.rsmanagement.core.module.users.role.domain.repository.RoleRepository;
import ck4.nvb.rsmanagement.core.module.users.role.service.RoleService;
import ck4.nvb.rsmanagement.core.module.users.role.service.dto.RoleDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import java.util.Map;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("roleService")
public class RoleServiceImpl
    extends FullAuditedCrudServiceImpl<RoleDto, Role, Long, UserGetDto, Long>
    implements RoleService {

  private final ModelMapper modelMapper = new ModelMapper();

  protected RoleServiceImpl(RoleRepository repository) {
    super(repository, Role.class);
  }

  @Override
  public RoleDto mapToEntityDto(Role entity) {
    return modelMapper.map(entity, RoleDto.class);
  }

  @Override
  public RoleRepository getRepository() {
    return (RoleRepository) super.getRepository();
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("name", List.of(SearchOperator.EQUALS));
    return keys;
  }
}
