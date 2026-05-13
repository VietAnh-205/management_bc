package ck4.nvb.rsmanagement.base.domain.entity;

import ck4.nvb.rsmanagement.base.domain.entity.interfaces.FullAudited;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Generated;

@MappedSuperclass
public abstract class FullAuditedGeneratedIdEntity extends AuditedGeneratedIdEntity
        implements FullAudited<Long> {
  // Fields
  @Column(name = "deleted_at")
  private LocalDateTime deletedTime;

  @Column(name = "deleted_by")
  private Long deleterId;

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
  public Long getDeleterID() {
    return deleterId;
  }

  @Generated
  public void setDeleterID(Long deleterID) {
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