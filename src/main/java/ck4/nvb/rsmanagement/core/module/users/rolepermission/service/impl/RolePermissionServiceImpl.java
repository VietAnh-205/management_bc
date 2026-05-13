package ck4.nvb.rsmanagement.core.module.users.rolepermission.service.impl;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.domain.entity.RolePermission;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.domain.repository.RolePermissionRepository;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.service.RolePermissionService;
import ck4.nvb.rsmanagement.core.module.users.rolepermission.service.dto.RolePermissionDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("rolePermissionService")
public class RolePermissionServiceImpl
    extends FullAuditedCrudServiceImpl<RolePermissionDto, RolePermission, Long, UserGetDto, Long>
    implements RolePermissionService {

  private final ModelMapper modelMapper = new ModelMapper();

  protected RolePermissionServiceImpl(RolePermissionRepository repository) {
    super(repository, RolePermission.class);
  }

  @Override
  public RolePermissionDto mapToEntityDto(RolePermission entity) {
    return modelMapper.map(entity, RolePermissionDto.class);
  }

  @Override
  public RolePermissionRepository getRepository() {
    return (RolePermissionRepository) super.getRepository();
  }
}
