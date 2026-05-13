package ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.service.dto.BatchStockGetDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("importLogRepository")
public interface BatchStockRepository extends BaseFullAuditedRepository<BatchStock, Long, Long> {

  @Query(
      value =
          """
                SELECT COALESCE(SUM(bi.original_qty), 0)
                FROM batch_stock bs
                JOIN batch b ON bs.batch_id = b.id
                JOIN batch_item bi ON bi.batch_id = b.id
                WHERE bi.product_id = :productId
                  AND bs.store_id = :storeId
                  AND bs.status = 'ACTIVE'
                  AND bs.deleted = false
                  AND b.deleted = false
                  AND bi.deleted = false
                """,
      nativeQuery = true)
  Long getTotalAvailableQuantityByProductAndStore(
      @Param("productId") Long productId, @Param("storeId") Long storeId);

  /** Find batches (batch_stock rows) that contain product in store, ordered by expiry (FIFO) */
  @Query(
      value =
          """
                SELECT bs.*
                FROM batch_stock bs
                JOIN batch b ON bs.batch_id = b.id
                JOIN batch_item bi ON bi.batch_id = b.id
                WHERE bi.product_id = :productId
                  AND bs.store_id = :storeId
                  AND bs.status = 'ACTIVE'
                  AND bs.deleted = false
                  AND b.deleted = false
                  AND bi.deleted = false
                ORDER BY bi.expiry_date ASC
                """,
      nativeQuery = true)
  List<BatchStock> findAvailableBatchStocksByProductAndStore(
      @Param("productId") Long productId, @Param("storeId") Long storeId);

  /** Find available batch stock infos for a product in a store, order by expiry date (FIFO) */
  @Query(
      value =
          """
    SELECT p.name as productName,
            bi.original_qty as qtyTotal,
            bi.original_qty as qtyAvailable,
            0 as qtyReversed,
            b.batch_code as batchCode,
            spl.name as supplierName,
            bi.import_price as importedPrice,
            bi.manufacture_date as manufactureDate,
            bi.expiry_date as expiryDate
    FROM batch_stock bs
    JOIN batch b ON b.id = bs.batch_id
    JOIN batch_item bi ON bi.batch_id = b.id
    JOIN product p ON p.id = bi.product_id
    JOIN supplier spl ON spl.id = bi.supplier_id
    WHERE bi.product_id = :productId
        AND bs.store_id = :storeId
        AND bs.status = 'ACTIVE'
        AND bs.deleted = false
        AND b.deleted = false
        AND bi.deleted = false
    ORDER BY bi.expiry_date ASC
    """,
      nativeQuery = true)
  List<BatchStockGetDto> findAvailableBatchInfoByProductAndStore(
      @Param("productId") Long productId, @Param("storeId") Long storeId);

  /** Find batch stocks that are about to expire (for inventory management) */
  @Query(
      value =
          """
                SELECT bs.*
                FROM batch_stock bs
                JOIN batch b ON bs.batch_id = b.id
                JOIN batch_item bi ON bi.batch_id = b.id
                WHERE bs.store_id = :storeId
                  AND bs.status = 'ACTIVE'
                  AND bs.deleted = false
                  AND b.deleted = false
                  AND bi.deleted = false
                  AND bi.expiry_date <= :expiryThreshold
                ORDER BY bi.expiry_date ASC
                """,
      nativeQuery = true)
  List<BatchStock> findExpiringBatchStocks(
      @Param("storeId") Long storeId,
      @Param("expiryThreshold") java.time.LocalDateTime expiryThreshold);

  /** Find batch stocks by batch_id and store ID */
  @Query(
      value =
          """
                SELECT *
                FROM batch_stock bs
                WHERE bs.batch_id = :batchId
                  AND bs.store_id = :storeId
                  AND bs.deleted = false
                """,
      nativeQuery = true)
  BatchStock findByBatchIdAndStoreId(
      @Param("batchId") Long batchId, @Param("storeId") Long storeId);

  /** Find batch stocks with low inventory (below threshold) */
  @Query(
      value =
          """
                SELECT bs.*
                FROM batch_stock bs
                JOIN batch b ON bs.batch_id = b.id
                JOIN batch_item bi ON bi.batch_id = b.id
                JOIN product p ON bi.product_id = p.id
                WHERE bs.store_id = :storeId
                  AND bs.status = 'ACTIVE'
                  AND bs.deleted = false
                  AND b.deleted = false
                  AND p.deleted = false
                  AND bi.deleted = false
                  AND bi.original_qty <= :threshold
                ORDER BY bi.original_qty ASC
                """,
      nativeQuery = true)
  List<BatchStock> findLowInventoryBatchStocks(
      @Param("storeId") Long storeId, @Param("threshold") Integer threshold);

  Optional<BatchStock> findFirstByBatchIdAndStoreId(Long batchId, Long storeId);
}
