package ck4.nvb.rsmanagement.core.module.users.permission.service.impl;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.users.permission.domain.entity.Permission;
import ck4.nvb.rsmanagement.core.module.users.permission.domain.repository.PermissionRepository;
import ck4.nvb.rsmanagement.core.module.users.permission.service.PermissionService;
import ck4.nvb.rsmanagement.core.module.users.permission.service.dto.PermissionDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import java.util.Map;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("permissionService")
public class PermissionServiceImpl
    extends FullAuditedCrudServiceImpl<PermissionDto, Permission, Long, UserGetDto, Long>
    implements PermissionService {

  private final ModelMapper modelMapper = new ModelMapper();

  protected PermissionServiceImpl(PermissionRepository repository) {
    super(repository, Permission.class);
  }

  @Override
  public PermissionDto mapToEntityDto(Permission entity) {
    return modelMapper.map(entity, PermissionDto.class);
  }

  @Override
  public PermissionRepository getRepository() {
    return (PermissionRepository) super.getRepository();
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("code", List.of(SearchOperator.EQUALS));
    return keys;
  }
}
