package ck4.nvb.rsmanagement.core.module.order.report;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "product_weekly_sales_audit")
public class ProductWeeklySalesAudit extends FullAuditedSerialIdEntity {

  private Long productWeeklySalesId;
  private Long storeId;
  private Long productId;
  private LocalDate weekStart;
  private Long deltaQty;
  private Long deltaRevenue;
  private String changeType;
  private String referenceId;
}
