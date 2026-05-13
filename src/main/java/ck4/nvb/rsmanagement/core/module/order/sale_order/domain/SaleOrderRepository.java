package ck4.nvb.rsmanagement.core.module.order.sale_order.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("orderRepository")
public interface SaleOrderRepository extends BaseFullAuditedRepository<SaleOrder, String, Long> {

  int countOrdersByCreatedTimeBetween(LocalDateTime from, LocalDateTime to);

  int countSaleOrdersByCreatedTimeBetweenAndStoreId(
      LocalDateTime from, LocalDateTime to, Long storeId);

  // --- tổng doanh thu của tất cả cửa hàng trong khoảng time ---
  @Query(
      value =
          """
                    SELECT COALESCE(SUM(so.final_price), 0)
                    FROM sale_order so
                    WHERE so.deleted = false
                      AND so.created_at >= :from
                      AND so.created_at < :to
                """,
      nativeQuery = true)
  Long sumTotalFinalPriceBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  // --- tổng doanh thu của 1 cửa hàng trong khoảng time ---
  @Query(
      value =
          """
                    SELECT COALESCE(SUM(so.final_price), 0)
                    FROM sale_order so
                    WHERE so.deleted = false
                      AND so.store_id = :storeId
                      AND so.created_at >= :from
                      AND so.created_at < :to
                """,
      nativeQuery = true)
  Long sumTotalFinalPriceOfAStoreBetween(
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      @Param("storeId") Long storeId);

  // final price by order id (giữ nguyên)
  @Query(
      value =
          """
                    SELECT COALESCE(SUM(sl.qty_ordered * sl.unit_price), 0)
                    FROM sale_line sl
                    WHERE sl.sale_order_id = :saleOrderId
                """,
      nativeQuery = true)
  int getFinalPriceByOrderId(@Param("saleOrderId") String saleOrderId);

  @Query(
      value =
          """
      SELECT CAST(so.created_at AS date) AS day, COALESCE(SUM(so.final_price), 0) AS revenue
      FROM sale_order so
      WHERE so.deleted = false
        AND so.store_id = :storeId
        AND so.created_at >= :from
        AND so.created_at < :to
      GROUP BY day
      ORDER BY day
    """,
      nativeQuery = true)
  List<Object[]> sumDailyRevenueOfAStoreBetween(
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      @Param("storeId") Long storeId);

  @Query(
      value =
          """
      SELECT s.id AS store_id, s.name AS store_name, CAST(so.created_at AS date) AS day,
             COALESCE(SUM(so.final_price), 0) AS revenue
      FROM sale_order so
      JOIN store s ON so.store_id = s.id
      WHERE so.deleted = false
        AND so.created_at >= :from
        AND so.created_at < :to
      GROUP BY s.id, s.name, day
      ORDER BY s.id, day
    """,
      nativeQuery = true)
  List<Object[]> sumDailyRevenueAllStoresBetween(
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  // --- RFM helpers (real DB queries) ---
  @Query(
      value =
          "SELECT CASE WHEN MAX(so.created_at) IS NULL THEN NULL ELSE EXTRACT(DAY FROM (NOW() - MAX(so.created_at))) END FROM sale_order so WHERE so.customer_id = :customerId AND so.deleted = false",
      nativeQuery = true)
  Integer getDaysSinceLastOrder(@Param("customerId") Long customerId);

  @Query(
      value =
          "SELECT COUNT(*) FROM sale_order so WHERE so.customer_id = :customerId AND so.created_at >= (NOW() - INTERVAL '12 months') AND so.deleted = false",
      nativeQuery = true)
  Integer getOrderCountLast12Months(@Param("customerId") Long customerId);

  @Query(
      value =
          "SELECT COALESCE(SUM(so.final_price),0) FROM sale_order so WHERE so.customer_id = :customerId AND so.created_at >= (NOW() - INTERVAL '12 months') AND so.deleted = false",
      nativeQuery = true)
  Long getTotalSpentLast12Months(@Param("customerId") Long customerId);
//
  @Query(
          value =
                  """
              SELECT so.store_id AS store_id,
                     s.name AS store_name,
                     DATE_SUB(DATE(so.created_at), INTERVAL WEEKDAY(so.created_at) DAY) AS week_start,
                     COALESCE(SUM(so.final_price), 0) AS revenue
              FROM sale_order so
              JOIN store s ON so.store_id = s.id
              WHERE so.deleted = false
                AND so.created_at >= :from
                AND so.created_at < :to
              GROUP BY so.store_id, s.name, week_start
              ORDER BY so.store_id, week_start
            """,
          nativeQuery = true)
  List<Object[]> sumWeeklyRevenueAllStoresBetween(
          @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  @Query(
          value =
                  """
              SELECT so.store_id AS store_id,
                     s.name AS store_name,
                     DATE_SUB(DATE(so.created_at), INTERVAL WEEKDAY(so.created_at) DAY) AS week_start,
                     COALESCE(SUM(so.final_price), 0) AS revenue
              FROM sale_order so
              JOIN store s ON so.store_id = s.id
              WHERE so.deleted = false
                AND so.created_at >= :from
                AND so.created_at < :to
                AND so.store_id = :storeId
              GROUP BY so.store_id, s.name, week_start
              ORDER BY week_start
            """,
          nativeQuery = true)
  List<Object[]> sumWeeklyRevenueOfAStoreBetween(
          @Param("from") LocalDateTime from,
          @Param("to") LocalDateTime to,
          @Param("storeId") Long storeId);

  @Query(
          value =
                  """
              SELECT so.store_id AS store_id,
                     s.name AS store_name,
                     DATE_SUB(DATE(so.created_at), INTERVAL WEEKDAY(so.created_at) DAY) AS week_start,
                     COALESCE(SUM(so.final_price), 0) AS revenue
              FROM sale_order so
              JOIN store s ON so.store_id = s.id
              WHERE so.deleted = false
                AND so.created_at >= :from
                AND so.created_at < :to
                AND so.store_id IN (:storeIds)
              GROUP BY so.store_id, s.name, week_start
              ORDER BY so.store_id, week_start
            """,
          nativeQuery = true)
  List<Object[]> sumWeeklyRevenueForStoresBetween(
          @Param("from") LocalDateTime from,
          @Param("to") LocalDateTime to,
          @Param("storeIds") List<Long> storeIds);

}
