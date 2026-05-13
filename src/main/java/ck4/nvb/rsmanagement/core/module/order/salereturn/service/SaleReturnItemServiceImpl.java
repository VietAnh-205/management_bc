package ck4.nvb.rsmanagement.core.module.order.salereturn.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.core.module.order.salereturn.domain.SaleReturnItem;
import ck4.nvb.rsmanagement.core.module.order.salereturn.domain.SaleReturnItemRepository;
import ck4.nvb.rsmanagement.core.module.order.salereturn.service.dto.SaleReturnItemDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("saleReturnItemService")
public class SaleReturnItemServiceImpl
    extends FullAuditedCrudServiceImpl<SaleReturnItemDto, SaleReturnItem, Long, UserGetDto, Long>
    implements ISaleReturnItemService {

  @Autowired private ModelMapper modelMapper;

  protected SaleReturnItemServiceImpl(SaleReturnItemRepository repository) {
    super(repository, SaleReturnItem.class);
  }

  @Override
  public SaleReturnItemRepository getRepository() {
    return (SaleReturnItemRepository) super.getRepository();
  }

  @Override
  public SaleReturnItemDto mapToEntityDto(SaleReturnItem entity) {
    return modelMapper.map(entity, SaleReturnItemDto.class);
  }
}
