package ck4.nvb.rsmanagement.core.module.stores.transfer.domain;

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
@Table(name = "store_transfer")
public class StoreTransfer extends FullAuditedSerialIdEntity {
  @Column(name = "from_store_id")
  private Long fromStoreId;

  @Column(name = "to_store_id")
  private Long toStoreId;

  @Column(name = "transfer_date")
  private LocalDateTime transferDate;

  @Column(name = "status")
  private String status;
}
