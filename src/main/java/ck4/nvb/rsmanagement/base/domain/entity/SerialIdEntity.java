package ck4.nvb.rsmanagement.base.domain.entity;

import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.util.JacksonParser;
import ck4.nvb.rsmanagement.base.util.SnowflakeIdGeneratorHolder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
@Access(AccessType.FIELD)
@SuperBuilder
public abstract class SerialIdEntity implements IEntity<Long> {

  // Fields
  @Id
  @Column(name = "id")
  private Long id;

  @PrePersist
  public void ensureId() {
    if (id == null) {
      id = SnowflakeIdGeneratorHolder.getInstance().nextId();
    }
  }

  public SerialIdEntity(Long serialId) {
    setId(serialId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (!(obj instanceof SerialIdEntity)) {
      return false;
    }

    if (this == obj) {
      return true;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    return this.getId() != null && this.getId().equals(((SerialIdEntity) obj).getId());
  }

  @Transient private boolean newEntity = false;

  @Override
  public void setNew(boolean newEntity) {
    this.newEntity = newEntity;
  }

  @Override
  public boolean isNew() {
    return newEntity;
  }

  @Override
  public String toString() {
    return super.getClass().getSimpleName() + " " + JacksonParser.getInstance().toJson(this);
  }
}
