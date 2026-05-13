package ck4.nvb.rsmanagement.core.module.order.salereturn.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.order.salereturn.domain.SaleReturnItem;
import ck4.nvb.rsmanagement.core.module.order.salereturn.service.dto.SaleReturnItemDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

public interface ISaleReturnItemService
    extends FullAuditedCrudService<SaleReturnItemDto, SaleReturnItem, Long, UserGetDto, Long> {}
