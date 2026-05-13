package ck4.nvb.rsmanagement.core.module.stores.transfer.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.stores.transfer.domain.StoreTransfer;
import ck4.nvb.rsmanagement.core.module.stores.transfer.service.dto.StoreTransferDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

public interface IStoreTransferService
    extends FullAuditedCrudService<StoreTransferDto, StoreTransfer, Long, UserGetDto, Long> {}
