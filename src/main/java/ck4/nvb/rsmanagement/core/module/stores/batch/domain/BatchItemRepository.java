package ck4.nvb.rsmanagement.core.module.stores.batch.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("batchItemRepository")
public interface BatchItemRepository extends BaseFullAuditedRepository<BatchItem, Long, Long> {
  List<BatchItem> findByProductId(Long productId);

  List<BatchItem> findByProductIdIn(List<Long> productIds);

  List<BatchItem> findByBatchId(Long batchId);

  List<BatchItem> findByBatchIdIn(List<Long> batchIds);

  List<BatchItem> findByBatchIdAndProductId(Long batchId, Long productId);

  List<BatchItem> findByBatchIdAndProductIdAndDeletedIsFalse(Long batchId, Long productId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT bi FROM BatchItem bi WHERE bi.id = :id")
  BatchItem findByIdForUpdate(@Param("id") Long id);

  @Query(
      value =
          """
            SELECT bi.*
            FROM batch_item bi
            JOIN batch b ON b.id = bi.batch_id
            JOIN batch_stock bs ON bs.batch_id = b.id
            WHERE bi.product_id = :productId
              AND bs.store_id = :storeId
              AND bs.status = 'ACTIVE'
              AND bi.deleted = false
              AND bs.deleted = false
              AND b.deleted = false
            ORDER BY bi.expiry_date ASC """,
      nativeQuery = true)
  List<BatchItem> findAvailableByProductAndStoreOrdered(Long productId, Long storeId);

  @Query(
      value =
          """
    SELECT COALESCE(SUM(bi.original_qty) - SUM(COALESCE(sa_sum.sold_qty, 0)), 0) AS available
    FROM batch_item bi
    JOIN batch b ON b.id = bi.batch_id
    JOIN batch_stock bs ON bs.batch_id = b.id
    LEFT JOIN (
        SELECT bi2.id AS batch_item_id, SUM(sa.sold_qty) AS sold_qty
        FROM sale_allocation sa
        JOIN sale_line sl ON sa.sale_line_id = sl.id
        JOIN batch_stock bs2 ON sa.batch_stock_id = bs2.id
        JOIN batch b2 ON bs2.batch_id = b2.id
        JOIN batch_item bi2 ON bi2.batch_id = b2.id AND bi2.product_id = sl.product_id
        WHERE sa.deleted = false
        GROUP BY bi2.id
    ) sa_sum ON sa_sum.batch_item_id = bi.id
    WHERE bi.product_id = :productId
      AND bs.store_id = :storeId
      AND bs.status = 'ACTIVE'
      AND bi.deleted = false
      AND bs.deleted = false
      AND b.deleted = false
    """,
      nativeQuery = true)
  Long getTotalAvailableQtyForProductInStore(
      @Param("productId") Long productId, @Param("storeId") Long storeId);
}
