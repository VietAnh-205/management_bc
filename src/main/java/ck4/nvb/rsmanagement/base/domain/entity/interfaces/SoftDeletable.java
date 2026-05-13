package ck4.nvb.rsmanagement.base.domain.entity.interfaces;

public interface SoftDeletable {

  /**
   * @return Boolean - whether of not this object is deleted
   */
  public boolean isDeleted();

  /**
   * @param deleted - whether of not this object is deleted
   */
  public void setDeleted(boolean deleted);
}
