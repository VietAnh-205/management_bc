package ck4.nvb.rsmanagement.core.module.order.voucher.domain;

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
@Table(name = "voucher_customer")
public class VoucherCustomer extends FullAuditedSerialIdEntity {
  @Column(name = "voucher_id")
  private Long voucherId;

  @Column(name = "customer_id")
  private Long customerId;

  @Column(name = "issued")
  private Boolean issued;
}
