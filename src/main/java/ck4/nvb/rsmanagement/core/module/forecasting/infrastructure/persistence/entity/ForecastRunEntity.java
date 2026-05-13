package ck4.nvb.rsmanagement.core.module.forecasting.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "forecast_run")
public class ForecastRunEntity {

  @Id
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "store_id", nullable = false)
  private Long storeId;

  @Column(name = "engine_type", nullable = false, length = 32)
  private String engineType;

  @Column(name = "history_days", nullable = false)
  private Integer historyDays;

  @Column(name = "horizon_days", nullable = false)
  private Integer horizonDays;

  @Column(name = "season_length", nullable = false)
  private Integer seasonLength;

  @Column(name = "generated_at", nullable = false)
  private Instant generatedAt;

  @Column(name = "train_from", nullable = false)
  private LocalDate trainFrom;

  @Column(name = "train_to", nullable = false)
  private LocalDate trainTo;
}