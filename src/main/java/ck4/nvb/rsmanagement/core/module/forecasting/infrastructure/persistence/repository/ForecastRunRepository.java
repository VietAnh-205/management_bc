package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.persistence.repository;

import ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.persistence.entity.ForecastRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForecastRunRepository extends JpaRepository<ForecastRunEntity, Long> {}