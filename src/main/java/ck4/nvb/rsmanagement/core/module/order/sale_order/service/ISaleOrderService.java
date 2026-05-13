package ck4.nvb.rsmanagement.core.module.order.sale_order.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleOrder;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto.SaleOrderGetFullDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

public interface ISaleOrderService
        extends FullAuditedCrudService<SaleOrderGetFullDto, SaleOrder, String, UserGetDto, Long> {}