package ck4.nvb.rsmanagement.core.module.order.salereturn.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.order.salereturn.domain.SaleReturn;
import ck4.nvb.rsmanagement.core.module.order.salereturn.service.dto.SaleReturnGetDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

public interface ISaleReturnService
    extends FullAuditedCrudService<SaleReturnGetDto, SaleReturn, Long, UserGetDto, Long> {}
