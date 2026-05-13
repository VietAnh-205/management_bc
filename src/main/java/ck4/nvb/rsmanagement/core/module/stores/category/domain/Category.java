package ck4.nvb.rsmanagement.core.module.stores.category.domain;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "category")
public class Category extends FullAuditedSerialIdEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;
}
