package ck4.nvb.rsmanagement.base.domain.entity;

import ck4.nvb.rsmanagement.base.domain.entity.interfaces.CreationAudited;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class CreationAuditedGeneratedIdEntity extends GeneratedIdEntity
        implements CreationAudited<Long> {

  // Fields
  @Column(name = "created_at")
  private LocalDateTime createdTime;

  @Column(name = "created_by")
  private Long creatorId;
}