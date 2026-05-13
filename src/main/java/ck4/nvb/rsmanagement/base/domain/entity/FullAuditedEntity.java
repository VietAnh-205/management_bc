package ck4.nvb.rsmanagement.base.domain.entity;

import ck4.nvb.rsmanagement.base.domain.entity.interfaces.FullAudited;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Generated;

@MappedSuperclass
public abstract class FullAuditedEntity<
        ID extends Comparable<ID> & Serializable, UID extends Comparable<UID> & Serializable>
    extends AuditedEntity<ID, UID> implements FullAudited<UID>, SoftDeletable {
  // Fields
  @Column(name = "deleted_time")
  private LocalDateTime deletedTime;

  @Column(name = "deleter_id")
  private UID deleterId;

  @Column(name = "deleted")
  private boolean deleted = false;

  @Generated
  public LocalDateTime getDeletedTime() {
    return deletedTime;
  }

  @Generated
  public void setDeletedTime(LocalDateTime deletedTime) {
    this.deletedTime = deletedTime;
  }

  @Generated
  public UID getDeleterID() {
    return deleterId;
  }

  @Generated
  public void setDeleterID(UID deleterID) {
    this.deleterId = deleterID;
  }

  @Generated
  public boolean isDeleted() {
    return deleted;
  }

  @Generated
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}
