package ck4.nvb.rsmanagement.core.module.order.salereturn.domain;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_return")
public class SaleReturn extends FullAuditedSerialIdEntity {
  @Column(name = "return_code")
  private String returnCode;

  @Column(name = "original_sale_order_id")
  private String originalSaleOrderId;

  @Column(name = "store_id")
  private Long storeId;

  @Column(name = "customer_id")
  private Long customerId;

  @Column(name = "return_reason")
  private String returnReason;

  @Column(name = "total_return_amount")
  private Integer totalReturnAmount;

  @Column(name = "refund_method")
  private String refundMethod;

  @Column(name = "is_processed")
  private Boolean isProcessed;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  @Column(name = "processed_by")
  private Long processedBy;
}
