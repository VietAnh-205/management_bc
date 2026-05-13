package ck4.nvb.rsmanagement.core.module.order.sale_order.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("saleAllocationRepository")
public interface SaleAllocationRepository
    extends BaseFullAuditedRepository<SaleAllocation, Long, Long> {

  /** Find all allocations for a specific order */
  @Query(
      """
        SELECT sa
        FROM SaleAllocation sa
        JOIN SaleLine sl ON sa.saleLineId = sl.id
        WHERE sl.saleOrderId = :orderId
          AND sa.deleted = false
          AND sl.deleted = false
        ORDER BY sa.id
      """)
  List<SaleAllocation> findAllocationsByOrderId(@Param("orderId") String orderId);

  /**
   * Sum sold_qty for a specific batch_item.
   *
   * <p>We can't rely on sale_allocation.batch_item_id (not present in new schema), so we join
   * sale_allocation -> sale_line -> batch_stock -> batch -> batch_item and match the target
   * batch_item.id.
   */
  @Query(
      value =
          """
    SELECT COALESCE(SUM(sa.sold_qty), 0)
    FROM sale_allocation sa
    JOIN sale_line sl ON sa.sale_line_id = sl.id
    JOIN batch_stock bs ON sa.batch_stock_id = bs.id
    JOIN batch b ON bs.batch_id = b.id
    JOIN batch_item bi ON bi.batch_id = b.id AND bi.product_id = sl.product_id
    WHERE bi.id = :batchItemId
      AND sa.deleted = false
      AND sl.deleted = false
      AND bi.deleted = false
    """,
      nativeQuery = true)
  Integer sumSoldQtyByBatchItemId(@Param("batchItemId") Long batchItemId);

  /**
   * Sum sold_qty for a given batch_stock and product. Join through sale_line to ensure allocation
   * belongs to the product.
   */
  @Query(
      value =
          """
    SELECT COALESCE(SUM(sa.sold_qty), 0)
    FROM sale_allocation sa
    JOIN sale_line sl ON sa.sale_line_id = sl.id
    JOIN batch_stock bs ON sa.batch_stock_id = bs.id
    JOIN batch b ON bs.batch_id = b.id
    JOIN batch_item bi ON bi.batch_id = b.id AND bi.product_id = sl.product_id
    WHERE bs.id = :batchStockId
      AND sl.product_id = :productId
      AND sa.deleted = false
      AND sl.deleted = false
      AND bi.deleted = false
    """,
      nativeQuery = true)
  Integer sumSoldQtyByBatchStockAndProduct(
      @Param("batchStockId") Long batchStockId, @Param("productId") Long productId);
}
