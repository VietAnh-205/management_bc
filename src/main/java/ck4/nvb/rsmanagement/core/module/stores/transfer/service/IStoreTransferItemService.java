package ck4.nvb.rsmanagement.core.module.stores.transfer.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.stores.transfer.domain.StoreTransferItem;
import ck4.nvb.rsmanagement.core.module.stores.transfer.service.dto.StoreTransferItemDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

public interface IStoreTransferItemService
    extends FullAuditedCrudService<
        StoreTransferItemDto, StoreTransferItem, Long, UserGetDto, Long> {}
