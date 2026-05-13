package ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.stereotype.Repository;

@Repository("InventoryAdjustmentRepository")
public interface InventoryAdjustmentRepository
    extends BaseFullAuditedRepository<InventoryAdjustment, Long, Long> {}
