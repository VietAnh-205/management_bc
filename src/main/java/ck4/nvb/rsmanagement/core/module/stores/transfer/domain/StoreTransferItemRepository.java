package ck4.nvb.rsmanagement.core.module.stores.transfer.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.stereotype.Repository;

@Repository("storeTransferItemRepository")
public interface StoreTransferItemRepository
    extends BaseFullAuditedRepository<StoreTransferItem, Long, Long> {}
