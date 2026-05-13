package ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.domain;

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
@Table(name = "inventory_adjustment")
public class InventoryAdjustment extends FullAuditedSerialIdEntity {

  @Column(name = "batch_stock_id")
  private Long batchStockId;

  @Column(name = "change_quantity")
  private Integer changeQuantity;

  @Column(name = "reason")
  private String reason;
}
