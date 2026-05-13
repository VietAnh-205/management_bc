package ck4.nvb.rsmanagement.base.domain.entity.interfaces;

import java.io.Serializable;

public interface CreationAudited<UID extends Comparable<UID> & Serializable>
    extends HasCreatedTime {

  /**
   * @return UID - the unique Identifier of user who created the object
   */
  UID getCreatorId();

  /**
   * @param creatorId - the unique Identifier of user who created the object
   */
  void setCreatorId(UID creatorId);
}
