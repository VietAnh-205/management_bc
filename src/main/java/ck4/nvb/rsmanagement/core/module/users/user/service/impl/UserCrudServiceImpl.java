package ck4.nvb.rsmanagement.core.module.users.user.service.impl;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.core.module.users.user.domain.User;
import ck4.nvb.rsmanagement.core.module.users.user.domain.UserRepository;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.BaseUserDto;
import ck4.nvb.rsmanagement.core.web.util.CommonPasswordEncoder;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("userCrudService")
public class UserCrudServiceImpl
    extends FullAuditedCrudServiceImpl<BaseUserDto, User, Long, BaseUserDto, Long>
    implements FullAuditedCrudService<BaseUserDto, User, Long, BaseUserDto, Long> {

  private final ModelMapper modelMapper = new ModelMapper();
  private final CommonPasswordEncoder commonPasswordEncoder;

  protected UserCrudServiceImpl(
      UserRepository repository, CommonPasswordEncoder commonPasswordEncoder) {
    super(repository, User.class);
    this.commonPasswordEncoder = commonPasswordEncoder;
  }

  @Override
  public UserRepository getRepository() {
    return (UserRepository) super.getRepository();
  }

  @Override
  public BaseUserDto mapToEntityDto(User entity) {
    return modelMapper.map(entity, BaseUserDto.class);
  }

  @Override
  protected BaseUserDto createEntity(
      User entity) { // ma hoa password truoc khi luu entity vao database
    String encodedPassword = commonPasswordEncoder.encode(entity.getPassword());
    entity.setPassword(encodedPassword);
    return super.createEntity(entity);
  }
}
