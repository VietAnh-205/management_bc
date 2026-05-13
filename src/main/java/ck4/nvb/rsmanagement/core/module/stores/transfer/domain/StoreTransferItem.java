package ck4.nvb.rsmanagement.core.module.stores.transfer.domain;

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
@Table(name = "store_transfer_item")
public class StoreTransferItem extends FullAuditedSerialIdEntity {
  @Column(name = "transfer_id")
  private Long transferId;

  @Column(name = "batch_stock_id")
  private Long batchStockId;

  @Column(name = "qty_requested")
  private Integer qtyRequested;

  @Column(name = "qtY_transfered")
  private Integer qtyTransferred;
}
