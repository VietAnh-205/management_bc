package ck4.nvb.rsmanagement.core.module.stores.transfer.service;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.base.application.exception.DuplicateIdentifierException;
import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.core.module.stores.transfer.domain.StoreTransfer;
import ck4.nvb.rsmanagement.core.module.stores.transfer.domain.StoreTransferRepository;
import ck4.nvb.rsmanagement.core.module.stores.transfer.service.dto.StoreTransferDto;
import ck4.nvb.rsmanagement.core.module.stores.transfer.service.dto.StoreTransferItemDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("storeTransferService")
public class StoreTransferServiceImpl
    extends FullAuditedCrudServiceImpl<StoreTransferDto, StoreTransfer, Long, UserGetDto, Long>
    implements IStoreTransferService {

  protected StoreTransferServiceImpl(StoreTransferRepository storeTransferRepository) {
    super(storeTransferRepository, StoreTransfer.class);
  }

  @Autowired private IStoreTransferItemService storeTransferItemService;

  @Autowired private ModelMapper modelMapper;

  @Override
  public StoreTransferRepository getRepository() {
    return (StoreTransferRepository) super.getRepository();
  }

  @Override
  public StoreTransferDto mapToEntityDto(StoreTransfer entity) {
    return modelMapper.map(entity, StoreTransferDto.class);
  }

  @Override
  public StoreTransfer mapToEntity(CreateInput<StoreTransfer> createDto, UserGetDto user) {
    StoreTransfer storeTransfer = super.mapToEntity(createDto, user);
    StoreTransfer entity = createDto.mapToEntity();
    storeTransfer.setFromStoreId(entity.getFromStoreId());
    storeTransfer.setToStoreId(entity.getToStoreId());
    storeTransfer.setTransferDate(entity.getTransferDate());
    storeTransfer.setStatus(entity.getStatus());
    return storeTransfer;
  }

  @Override
  public StoreTransferDto create(CreateInput<StoreTransfer> createDto, UserGetDto user)
      throws AppException {
    if (createDto instanceof StoreTransferDto) {
      return create((StoreTransferDto) createDto, user);
    }
    return super.create(createDto, user);
  }

  private StoreTransferDto create(StoreTransferDto createDto, UserGetDto user) {
    super.checkCreatePermission(createDto, user);

    StoreTransfer storeTransfer = mapToEntity(createDto, user);

    if (storeTransfer.getId() != null && exists(storeTransfer.getId())) {
      getLogger().error("Duplicate id {}", storeTransfer.getId());
      throw new DuplicateIdentifierException("Duplicate identifier " + storeTransfer.getId());
    }

    storeTransfer = getRepository().save(storeTransfer);

    for (StoreTransferItemDto storeTransferItemDto : createDto.getItems()) {
      storeTransferItemDto.setTransferId(storeTransfer.getId());
      storeTransferItemService.create(storeTransferItemDto, user);
    }

    return mapToEntityDto(storeTransfer);
  }
}
