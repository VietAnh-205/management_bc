package ck4.nvb.rsmanagement.base.application.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CreationAuditedEntityDto<
        ID extends Comparable<ID> & Serializable, UID extends Comparable<UID> & Serializable>
    extends EntityDto<ID> {
  private UID uid;

  private LocalDateTime creationTime;
}
