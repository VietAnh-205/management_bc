package ck4.nvb.rsmanagement.core.module.stores.batch.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("batchRepository")
public interface BatchRepository extends BaseFullAuditedRepository<Batch, Long, Long> {

  // Lấy tất cả batch mà chứa productId thông qua batch_item
  @Query(
      value =
          """
        SELECT DISTINCT b.*
        FROM batch b
        JOIN batch_item bi ON bi.batch_id = b.id
        WHERE bi.product_id = :productId
          AND b.deleted = false
          AND bi.deleted = false
        """,
      nativeQuery = true)
  List<Batch> findByProductId(@Param("productId") Long productId);

  // Lấy batch cho nhiều productIds
  @Query(
      value =
          """
        SELECT DISTINCT b.*
        FROM batch b
        JOIN batch_item bi ON bi.batch_id = b.id
        WHERE bi.product_id IN (:productIds)
          AND b.deleted = false
          AND bi.deleted = false
        """,
      nativeQuery = true)
  List<Batch> findByProductIdIn(@Param("productIds") Collection<Long> productIds);
}
