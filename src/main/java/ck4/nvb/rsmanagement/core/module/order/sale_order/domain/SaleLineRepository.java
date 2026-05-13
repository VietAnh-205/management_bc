package ck4.nvb.rsmanagement.core.module.order.sale_order.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("orderDetailRepository")
public interface SaleLineRepository extends BaseFullAuditedRepository<SaleLine, Long, Long> {
  @Query(
      value = """
    SELECT *
    FROM sale_line
    WHERE sale_order_id = :saleOrderId
    """,
      nativeQuery = true)
  List<SaleLine> findBySaleOrderId(String saleOrderId);

  @Query(
      value =
          """
                SELECT p.id as id, p.sku as sku, p.name as name, p.description as description,
                       p.unit_price as unitPrice, p.category_id as categoryId,
                       top_sold.total_quantity as totalQuantitySold
                FROM product p
                JOIN (
                    SELECT sl.product_id, SUM(sl.qty_ordered) AS total_quantity \s
                    FROM sale_line sl
                    JOIN sale_order so ON sl.sale_order_id = so.id
                    WHERE so.created_at BETWEEN :start AND :end
                    GROUP BY sl.product_id
                    ORDER BY total_quantity DESC
                    LIMIT :numberOfProducts
                ) AS top_sold ON p.id = top_sold.product_id
                ORDER BY top_sold.total_quantity DESC
            """,
      nativeQuery = true)
  List<Map<String, Object>> findMostSoldProductsOfIntervalWithQty(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("numberOfProducts") int numberOfProducts);

  @Query(
      value =
          """
                SELECT p.id as id, p.sku as sku, p.name as name, p.description as description,
                       p.unit_price as unitPrice, p.category_id as categoryId,
                       top_sold.total_quantity as totalQuantitySold
                FROM product p
                JOIN (
                    SELECT sl.product_id, SUM(sl.qty_ordered) AS total_quantity \s
                    FROM sale_line sl
                    JOIN sale_order so ON sl.sale_order_id = so.id
                    WHERE so.created_at BETWEEN :start AND :end
                        AND so.store_id=:storeId
                    GROUP BY sl.product_id
                    ORDER BY total_quantity DESC
                    LIMIT :numberOfProducts
                ) AS top_sold ON p.id = top_sold.product_id
                ORDER BY top_sold.total_quantity DESC
            """,
      nativeQuery = true)
  List<Map<String, Object>> findMostSoldProductsOfIntervalWithQtyOfAStore(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("numberOfProducts") int numberOfProducts,
      @Param("storeId") Long storeId);

  //
  @Query(
      "SELECT sl.productId, SUM(sl.qtyOrdered) FROM SaleLine sl WHERE sl.deleted = false GROUP BY sl.productId")
  List<Object[]> sumQtyGroupedByProduct();

    @Query(value = """
    SELECT t.week_start,
           t.store_id,
           p.id AS id,
           p.sku AS sku,
           p.name AS name,
           p.description AS description,
           p.unit_price AS unitPrice,
           p.category_id AS categoryId,
           t.total_quantity AS totalQuantitySold
    FROM (
      SELECT s.week_start,
             s.store_id,
             s.product_id,
             s.total_quantity,
             ROW_NUMBER() OVER (
               PARTITION BY s.store_id, s.week_start
               ORDER BY s.total_quantity DESC
             ) AS rn
      FROM (
        SELECT DATE_SUB(DATE(so.created_at), INTERVAL WEEKDAY(so.created_at) DAY) AS week_start,
               so.store_id,
               sl.product_id,
               SUM(sl.qty_ordered) AS total_quantity
        FROM sale_line sl
        JOIN sale_order so ON sl.sale_order_id = so.id
        WHERE so.created_at BETWEEN :start AND :end
        GROUP BY week_start, so.store_id, sl.product_id
      ) s
    ) t
    JOIN product p ON p.id = t.product_id
    WHERE t.rn <= :numberOfProducts
    ORDER BY t.week_start DESC, t.store_id, t.total_quantity DESC
    """, nativeQuery = true)
    List<Map<String, Object>> findTopProductsPerStorePerWeek(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("numberOfProducts") int numberOfProducts);

    /**
     * Giống method trên nhưng filter cho một danh sách store cụ thể (IN (:storeIds)).
     */
    @Query(value = """
    SELECT t.week_start,
           t.store_id,
           p.id AS id,
           p.sku AS sku,
           p.name AS name,
           p.description AS description,
           p.unit_price AS unitPrice,
           p.category_id AS categoryId,
           t.total_quantity AS totalQuantitySold
    FROM (
      SELECT s.week_start,
             s.store_id,
             s.product_id,
             s.total_quantity,
             ROW_NUMBER() OVER (
               PARTITION BY s.store_id, s.week_start
               ORDER BY s.total_quantity DESC
             ) AS rn
      FROM (
        SELECT DATE_SUB(DATE(so.created_at), INTERVAL WEEKDAY(so.created_at) DAY) AS week_start,
               so.store_id,
               sl.product_id,
               SUM(sl.qty_ordered) AS total_quantity
        FROM sale_line sl
        JOIN sale_order so ON sl.sale_order_id = so.id
        WHERE so.created_at BETWEEN :start AND :end
          AND so.store_id IN (:storeIds)
        GROUP BY week_start, so.store_id, sl.product_id
      ) s
    ) t
    JOIN product p ON p.id = t.product_id
    WHERE t.rn <= :numberOfProducts
    ORDER BY t.week_start DESC, t.store_id, t.total_quantity DESC
    """, nativeQuery = true)
    List<Map<String, Object>> findTopProductsPerStorePerWeekForStores(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("numberOfProducts") int numberOfProducts,
            @Param("storeIds") List<Long> storeIds);

  /**
   * Cold products (ít bán nhất) per store per week — chỉ sản phẩm có sales (MySQL 8+, window function)
   * This is like findTopProductsPerStorePerWeek but order by total_quantity ASC
   */
  @Query(value = """
    SELECT t.week_start,
           t.store_id,
           p.id AS id,
           p.sku AS sku,
           p.name AS name,
           p.description AS description,
           p.unit_price AS unitPrice,
           p.category_id AS categoryId,
           t.total_quantity AS totalQuantitySold
    FROM (
      SELECT s.week_start,
             s.store_id,
             s.product_id,
             s.total_quantity,
             ROW_NUMBER() OVER (
               PARTITION BY s.store_id, s.week_start
               ORDER BY s.total_quantity ASC
             ) AS rn
      FROM (
        SELECT DATE_SUB(DATE(so.created_at), INTERVAL WEEKDAY(so.created_at) DAY) AS week_start,
               so.store_id,
               sl.product_id,
               SUM(sl.qty_ordered) AS total_quantity
        FROM sale_line sl
        JOIN sale_order so ON sl.sale_order_id = so.id
        WHERE so.created_at BETWEEN :start AND :end
        GROUP BY week_start, so.store_id, sl.product_id
      ) s
    ) t
    JOIN product p ON p.id = t.product_id
    WHERE t.rn <= :numberOfProducts
    ORDER BY t.week_start DESC, t.store_id, t.total_quantity ASC
    """, nativeQuery = true)
  List<Map<String, Object>> findColdProductsPerStorePerWeek(
          @Param("start") LocalDateTime start,
          @Param("end") LocalDateTime end,
          @Param("numberOfProducts") int numberOfProducts);

  @Query(value = """
    SELECT DATE_SUB(DATE(so.created_at), INTERVAL WEEKDAY(so.created_at) DAY) AS week_start,
           so.store_id AS store_id,
           sl.product_id AS product_id,
           SUM(sl.qty_ordered) AS total_quantity
    FROM sale_line sl
    JOIN sale_order so ON sl.sale_order_id = so.id
    WHERE so.created_at BETWEEN :start AND :end
    GROUP BY week_start, so.store_id, sl.product_id
    ORDER BY week_start DESC, so.store_id
    """, nativeQuery = true)
  List<Map<String, Object>> findSalesPerStorePerWeek(
          @Param("start") LocalDateTime start,
          @Param("end") LocalDateTime end);

  @Query(value = """
    SELECT DATE_SUB(DATE(so.created_at), INTERVAL WEEKDAY(so.created_at) DAY) AS week_start,
           so.store_id AS store_id,
           sl.product_id AS product_id,
           SUM(sl.qty_ordered) AS total_quantity
    FROM sale_line sl
    JOIN sale_order so ON sl.sale_order_id = so.id
    WHERE so.created_at BETWEEN :start AND :end
      AND so.store_id IN (:storeIds)
    GROUP BY week_start, so.store_id, sl.product_id
    ORDER BY week_start DESC, so.store_id
    """, nativeQuery = true)
  List<Map<String, Object>> findSalesPerStorePerWeekForStores(
          @Param("start") LocalDateTime start,
          @Param("end") LocalDateTime end,
          @Param("storeIds") List<Long> storeIds);

}
