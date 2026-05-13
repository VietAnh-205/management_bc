package ck4.nvb.rsmanagement.base.domain.entity.interfaces;

import java.io.Serializable;

public interface ModificationAudited<UID extends Comparable<UID> & Serializable>
    extends HasUpdatedTime {

  /**
   * @return UID - the identifier of the user who updated the object
   */
  UID getUpdaterID();

  /**
   * @param updaterID - the identifier of the user who updated the object
   */
  void setUpdaterID(UID updaterID);
}
