package ck4.nvb.rsmanagement.core.module.stores.transfer.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.core.module.stores.transfer.domain.StoreTransferItem;
import ck4.nvb.rsmanagement.core.module.stores.transfer.domain.StoreTransferItemRepository;
import ck4.nvb.rsmanagement.core.module.stores.transfer.service.dto.StoreTransferItemDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("storeTransferItemService")
public class StoreTransferItemServiceImpl
    extends FullAuditedCrudServiceImpl<
        StoreTransferItemDto, StoreTransferItem, Long, UserGetDto, Long>
    implements IStoreTransferItemService {

  protected StoreTransferItemServiceImpl(StoreTransferItemRepository storeTransferItemRepository) {
    super(storeTransferItemRepository, StoreTransferItem.class);
  }

  @Autowired private ModelMapper modelMapper;

  @Override
  public StoreTransferItemRepository getRepository() {
    return (StoreTransferItemRepository) super.getRepository();
  }

  @Override
  public StoreTransferItemDto mapToEntityDto(StoreTransferItem entity) {
    return modelMapper.map(entity, StoreTransferItemDto.class);
  }
}
