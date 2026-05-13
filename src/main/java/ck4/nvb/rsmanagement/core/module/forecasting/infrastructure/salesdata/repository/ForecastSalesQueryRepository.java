package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.salesdata.repository;

import ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.salesdata.projection.DailyStoreProductSalesRow;
// Thêm dòng import Entity này vào
import ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.persistence.entity.ForecastRunEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ForecastSalesQueryRepository extends Repository<ForecastRunEntity, Long> {

  @Query(
          value =
                  """
                  SELECT
                    so.store_id AS storeId,
                    sl.product_id AS productId,
                    DATE(so.created_at) AS saleDate,
                    SUM(sl.qty_ordered) AS qty
                  FROM sale_order so
                  JOIN sale_line sl ON sl.sale_order_id = so.id
                  WHERE so.store_id = :storeId
                    AND sl.product_id IN (:productIds)
                    AND so.created_at >= :fromInclusive
                    AND so.created_at <= :toInclusive
                  GROUP BY so.store_id, sl.product_id, DATE(so.created_at)
                  """,
          nativeQuery = true)
  List<DailyStoreProductSalesRow> findDailySalesByStoreAndProducts(
          @Param("storeId") Long storeId,
          @Param("productIds") Collection<Long> productIds,
          @Param("fromInclusive") LocalDateTime fromInclusive,
          @Param("toInclusive") LocalDateTime toInclusive);
}