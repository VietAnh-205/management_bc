package ck4.nvb.rsmanagement.core.module.stores.transfer.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.stereotype.Repository;

@Repository("storeTransferRepository")
public interface StoreTransferRepository
    extends BaseFullAuditedRepository<StoreTransfer, Long, Long> {}
