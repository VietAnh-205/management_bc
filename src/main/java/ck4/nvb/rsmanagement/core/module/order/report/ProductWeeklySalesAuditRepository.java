package ck4.nvb.rsmanagement.core.module.order.report;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductWeeklySalesAuditRepository
    extends JpaRepository<ProductWeeklySalesAudit, Long> {}
