package ck4.nvb.rsmanagement.base.domain.entity.interfaces;

import java.time.LocalDateTime;

public interface HasDeletedTime {

  /**
   * @return Date - the date the object was deleted
   */
  LocalDateTime getDeletedTime();

  /**
   * @param deletedTime - the date the object was deleted
   */
  void setDeletedTime(LocalDateTime deletedTime);
}
