package ck4.nvb.rsmanagement.core.module.order.salereturn.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("saleReturnRepository")
public interface SaleReturnRepository extends BaseFullAuditedRepository<SaleReturn, Long, Long> {
  @Query(
      value =
          """
      SELECT COALESCE(SUM(sr.total_return_amount), 0)
      FROM sale_return sr
      WHERE sr.deleted = false
        AND sr.store_id = :storeId
        AND sr.processed_at >= :from
        AND sr.processed_at < :to
    """,
      nativeQuery = true)
  Long sumTotalReturnAmountOfAStoreBetween(
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      @Param("storeId") Long storeId);
}
