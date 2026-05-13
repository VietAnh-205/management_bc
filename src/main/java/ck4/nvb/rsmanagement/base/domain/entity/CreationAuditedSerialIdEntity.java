package ck4.nvb.rsmanagement.base.domain.entity;

import ck4.nvb.rsmanagement.base.domain.entity.interfaces.CreationAudited;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CreationAuditedSerialIdEntity extends SerialIdEntity implements CreationAudited<Long> {

  @Column(name = "created_at")
  private LocalDateTime createdTime;

  @Column(name = "created_by")
  private Long creatorId;
}
