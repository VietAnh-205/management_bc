package ck4.nvb.rsmanagement.core.module.stores.supplier.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.stereotype.Repository;

@Repository("supplierRepository")
public interface SupplierRepository extends BaseFullAuditedRepository<Supplier, Long, Long> {}
