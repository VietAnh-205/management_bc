package ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository("paymentMethodRepository")
public interface PaymentMethodRepository
    extends BaseFullAuditedRepository<PaymentMethod, Long, Long> {

  @Query(
      value =
          """
        SELECT pm.name, COUNT(so.id) as paymentUsage
        FROM payment_method pm
        LEFT JOIN sale_order so ON pm.id = so.payment_id
            AND so.deleted = false
            AND so.created_at BETWEEN :start AND :end
        WHERE pm.deleted = false
        GROUP BY pm.name
        ORDER BY paymentUsage DESC
    """,
      nativeQuery = true)
  List<Map<String, Object>> findPaymentMethodUsageStats(LocalDateTime start, LocalDateTime end);
}
