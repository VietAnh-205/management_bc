package ck4.nvb.rsmanagement.core.module.order.sale_order.domain;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_line")
public class SaleLine extends FullAuditedSerialIdEntity {

  @Column(name = "sale_order_id")
  private String saleOrderId;

  @Column(name = "product_id")
  private Long productId;

  @Column(name = "qty_ordered")
  private Integer qtyOrdered;

  @Column(name = "qty_allocated")
  private Integer qtyAllocated;

  @Column(name = "qty_picked")
  private Integer qtyPicked;

  @Column(name = "unit_price")
  private Integer unitPrice;
}
