package ck4.nvb.rsmanagement.core.module.stores.store.domain;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "store")
public class Store extends FullAuditedSerialIdEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "address")
  private String address;

  @Column(name = "location_id")
  private Long locationId;

  @Column(name = "phone")
  private String phone;
}
