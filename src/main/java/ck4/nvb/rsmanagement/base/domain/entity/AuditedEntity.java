package ck4.nvb.rsmanagement.base.domain.entity;

import ck4.nvb.rsmanagement.base.domain.entity.interfaces.Audited;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Generated;

@MappedSuperclass
public abstract class AuditedEntity<
        ID extends Comparable<ID> & Serializable, UID extends Comparable<UID> & Serializable>
    extends CreationAuditedEntity<ID, UID> implements Audited<UID> {

  @Column(name = "last_updated")
  private LocalDateTime lastUpdated;

  @Column(name = "last_updater")
  private UID lastUpdater;

  public AuditedEntity() {}

  @Generated
  public LocalDateTime getLastUpdated() {
    return lastUpdated;
  }

  @Generated
  public UID getUpdaterID() {
    return lastUpdater;
  }

  @Generated
  public void setLastUpdated(LocalDateTime lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Generated
  public void setUpdaterID(UID lastUpdater) {
    this.lastUpdater = lastUpdater;
  }
}
