package ck4.nvb.rsmanagement.base.domain.entity.interfaces;

import java.io.Serializable;

public interface DeletionAudited<UID extends Comparable<UID> & Serializable>
    extends HasDeletedTime {

  /**
   * @return UID - the unique Identifier of user who deleted the object
   */
  UID getDeleterID();

  /**
   * @param deleterID - the unique Identifier of user who deleted the object
   */
  void setDeleterID(UID deleterID);

  /**
   * @return Boolean - whether of not this object is deleted
   */
  boolean isDeleted();

  /**
   * @param deleted - whether of not this object is deleted
   */
  void setDeleted(boolean deleted);
}
