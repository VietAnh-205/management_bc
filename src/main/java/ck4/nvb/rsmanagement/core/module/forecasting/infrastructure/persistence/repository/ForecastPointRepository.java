package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.persistence.repository;

import ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.persistence.entity.ForecastPointEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForecastPointRepository extends JpaRepository<ForecastPointEntity, Long> {

  List<ForecastPointEntity> findByRunIdAndStoreIdAndProductIdOrderByForecastDateAsc(
      Long runId, Long storeId, Long productId);

  List<ForecastPointEntity> findByStoreIdAndProductIdAndForecastDateBetweenOrderByForecastDateAsc(
      Long storeId, Long productId, LocalDate from, LocalDate to);
}