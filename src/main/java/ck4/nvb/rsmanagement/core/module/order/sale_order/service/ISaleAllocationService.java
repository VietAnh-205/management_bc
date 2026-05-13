package ck4.nvb.rsmanagement.core.module.order.sale_order.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleAllocation;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto.SaleAllocationDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

public interface ISaleAllocationService
    extends FullAuditedCrudService<SaleAllocationDto, SaleAllocation, Long, UserGetDto, Long> {
  /** Tổng sold_qty theo batch_item */
  Integer getTotalSoldQuantityByBatchItem(Long batchItemId);

  /** Tổng sold_qty theo batch_stock và product (dùng khi cần aggregate theo batch_stock) */
  Integer getTotalSoldQuantityByBatchStockAndProduct(Long batchStockId, Long productId);
}
