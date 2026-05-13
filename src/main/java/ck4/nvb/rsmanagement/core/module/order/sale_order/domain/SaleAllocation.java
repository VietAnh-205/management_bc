package ck4.nvb.rsmanagement.core.module.order.sale_order.domain;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_allocation")
public class SaleAllocation extends FullAuditedSerialIdEntity {

  @Column(name = "sale_line_id", nullable = false)
  private Long saleLineId;

  @Column(name = "batch_stock_id", nullable = false)
  private Long batchStockId;

  @Column(name = "sold_qty", nullable = false)
  private Integer soldQty;

  @Column(name = "unit_cost_snap", nullable = false)
  private Integer unitCostSnap;

  @Transient private Long batchItemId;
}
