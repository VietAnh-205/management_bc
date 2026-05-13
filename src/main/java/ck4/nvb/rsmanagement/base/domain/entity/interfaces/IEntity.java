package ck4.nvb.rsmanagement.base.domain.entity.interfaces;

import java.io.Serializable;
import org.springframework.data.domain.Persistable;

public interface IEntity<ID extends Comparable<ID> & Serializable>
    extends Persistable<ID>, Serializable {

  /**
   * @return id - The unique Identifier for the object
   */
  ID getId();

  /**
   * @param id - The unique Identifier for the object
   */
  void setId(ID id);

  /**
   * @param newEntity
   */
  void setNew(boolean newEntity);
}
