package ck4.nvb.rsmanagement.core.module.order.voucher.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.stereotype.Repository;

@Repository("voucherCustomerRepository")
public interface VoucherCustomerRepository
    extends BaseFullAuditedRepository<VoucherCustomer, Long, Long> {
  boolean existsByVoucherIdAndCustomerIdAndIssuedTrue(Long voucherId, Long customerId);
}
