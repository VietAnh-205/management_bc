package ck4.nvb.rsmanagement.core.module.order.customer.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.stereotype.Repository;

@Repository("customerRepository")
public interface CustomerRepository extends BaseFullAuditedRepository<Customer, Long, Long> {
  int countCustomerByDeletedIsFalse();

  String findCustomerById(Long id);
}
