package ck4.nvb.rsmanagement.base.domain.entity.interfaces;

import java.time.LocalDateTime;

public interface HasCreatedTime {

  /**
   * @return Date - the date the object was created
   */
  LocalDateTime getCreatedTime();

  /**
   * @param createdTime - the date the object was created
   */
  void setCreatedTime(LocalDateTime createdTime);
}
