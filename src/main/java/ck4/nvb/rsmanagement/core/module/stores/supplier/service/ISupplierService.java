package ck4.nvb.rsmanagement.core.module.stores.supplier.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.stores.supplier.domain.Supplier;
import ck4.nvb.rsmanagement.core.module.stores.supplier.service.dto.SupplierDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

public interface ISupplierService
    extends FullAuditedCrudService<SupplierDto, Supplier, Long, UserGetDto, Long> {}
