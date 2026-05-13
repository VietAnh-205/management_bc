package ck4.nvb.rsmanagement.core.module.order.report;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductWeeklySalesRepository extends JpaRepository<ProductWeeklySales, Long> {

  List<ProductWeeklySales> findByStoreIdAndWeekStartOrderByQtyDesc(
      Long storeId, LocalDate weekStart);

  List<ProductWeeklySales> findByStoreIdAndWeekStartOrderByQtyAsc(
      Long storeId, LocalDate weekStart);

  @Query(
      "SELECT pws.productId FROM ProductWeeklySales pws WHERE pws.storeId = :storeId AND pws.weekStart = :weekStart")
  List<Long> findProductIdsSoldInWeek(
      @Param("storeId") Long storeId, @Param("weekStart") LocalDate weekStart);
}
