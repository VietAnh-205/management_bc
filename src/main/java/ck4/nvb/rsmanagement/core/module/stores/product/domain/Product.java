package ck4.nvb.rsmanagement.core.module.stores.product.domain;

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
@Table(name = "product")
public class Product extends FullAuditedSerialIdEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "sku", nullable = false, unique = true)
  private String sku;

  @Column(name = "description")
  private String description;

  @Column(name = "unit_price", nullable = false)
  private Integer unitPrice;

  @Column(name = "category_id")
  private Long categoryId;
}
