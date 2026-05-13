package ck4.nvb.rsmanagement.base.domain.entity;

import ck4.nvb.rsmanagement.base.domain.entity.interfaces.CreationAudited;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class CreationAuditedEntity<
        ID extends Comparable<ID> & Serializable, UID extends Comparable<UID> & Serializable>
    extends IdEntity<ID> implements CreationAudited<UID> {

  @Column(name = "created_time", nullable = false, updatable = false)
  private LocalDateTime createdTime;

  @Column(name = "creator_id", nullable = false, updatable = false)
  private UID creatorId;
}
