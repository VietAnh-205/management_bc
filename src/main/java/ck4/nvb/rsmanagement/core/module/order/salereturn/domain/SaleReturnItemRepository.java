package ck4.nvb.rsmanagement.core.module.order.salereturn.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.stereotype.Repository;

@Repository("saleReturnItemRepository")
public interface SaleReturnItemRepository
    extends BaseFullAuditedRepository<SaleReturnItem, Long, Long> {}
