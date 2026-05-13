package ck4.nvb.rsmanagement.core.module.order.customer.domain;

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
@Table(name = "customer")
public class Customer extends FullAuditedSerialIdEntity {
  @Column(name = "name")
  private String name;

  @Column(name = "phone")
  private String phone;

  @Column(name = "gender")
  private String gender;

  @Column(name = "point")
  private Integer point;
}
