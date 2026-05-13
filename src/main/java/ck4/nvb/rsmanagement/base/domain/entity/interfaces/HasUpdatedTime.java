package ck4.nvb.rsmanagement.base.domain.entity.interfaces;

import java.time.LocalDateTime;

public interface HasUpdatedTime {

  /**
   * @return Date - the date the object was last changed
   */
  LocalDateTime getUpdatedTime();

  /**
   * @param updatedTime - the date the object was last changed
   */
  void setUpdatedTime(LocalDateTime updatedTime);
}
