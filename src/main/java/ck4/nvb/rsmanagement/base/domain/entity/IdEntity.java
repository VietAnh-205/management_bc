package ck4.nvb.rsmanagement.base.domain.entity;

import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.util.JacksonParser;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public class IdEntity<ID extends Comparable<ID> & Serializable> implements IEntity<ID> {

  // Fields
  @Id
  @Column(name = "id")
  private ID id;

  public IdEntity(ID id) {
    setId(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (!(obj instanceof IdEntity)) {
      return false;
    }

    if (this == obj) {
      return true;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    return this.getId() != null && this.getId().equals(((IdEntity<?>) obj).getId());
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
