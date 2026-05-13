package ck4.nvb.rsmanagement.core.module.stores.location.domain;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Entity
@Table(name = "location")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location extends FullAuditedSerialIdEntity {
  @Column(name = "longitude", precision = 11, scale = 8)
  private BigDecimal longitude;

  @Column(name = "latitude", precision = 10, scale = 8)
  private BigDecimal latitude;
}
