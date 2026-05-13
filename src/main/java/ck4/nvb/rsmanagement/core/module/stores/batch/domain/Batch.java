package ck4.nvb.rsmanagement.core.module.stores.batch.domain;

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
@Table(name = "batch")
public class Batch extends FullAuditedSerialIdEntity {

  @Column(name = "batch_code", nullable = false)
  private String batchCode;
}
