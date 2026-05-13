package ck4.nvb.rsmanagement.core.module.stores.batch.domain;

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
@Table(name = "batch_item")
public class BatchItem extends FullAuditedSerialIdEntity {

  @Column(name = "batch_id", nullable = false)
  private Long batchId;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "supplier_id", nullable = false)
  private Long supplierId;

  @Column(name = "original_qty", nullable = false)
  private Integer originalQty;

  @Column(name = "import_price", nullable = false)
  private Integer importPrice;

  @Column(name = "manufacture_date", nullable = false)
  private LocalDateTime manufactureDate;

  @Column(name = "expiry_date", nullable = false)
  private LocalDateTime expiryDate;
}
