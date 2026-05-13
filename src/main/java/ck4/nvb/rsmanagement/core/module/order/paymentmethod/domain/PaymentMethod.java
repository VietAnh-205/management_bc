package ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain;

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
@Table(name = "payment_method")
public class PaymentMethod extends FullAuditedSerialIdEntity {
  @Column(name = "code", nullable = false)
  private String code;

  @Column(name = "name")
  private String name;
}
