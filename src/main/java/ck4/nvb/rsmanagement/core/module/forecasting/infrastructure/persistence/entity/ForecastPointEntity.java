package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "forecast_point",
    indexes = {
      @Index(name = "idx_forecast_point_store_product_date", columnList = "store_id,product_id,forecast_date"),
      @Index(name = "idx_forecast_point_run_store_product", columnList = "run_id,store_id,product_id")
    })
public class ForecastPointEntity {

  @Id
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "run_id", nullable = false)
  private Long runId;

  @Column(name = "store_id", nullable = false)
  private Long storeId;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "forecast_date", nullable = false)
  private LocalDate forecastDate;

  @Column(name = "yhat", nullable = false)
  private Double yhat; // store double, API rounds to Integer
}