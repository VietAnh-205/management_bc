package ck4.nvb.rsmanagement.core.module.order.report;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(
    name = "product_weekly_sales",
    uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "product_id", "week_start"}))
public class ProductWeeklySales extends FullAuditedSerialIdEntity {

  @Column(name = "store_id", nullable = false)
  private Long storeId;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "week_start", nullable = false)
  private LocalDate weekStart;

  @Column(name = "qty", nullable = false)
  private Long qty = 0L;

  @Column(name = "revenue", nullable = false)
  private Long revenue = 0L;

  @Column(name = "finalized", nullable = false)
  private boolean finalized = false;
}
