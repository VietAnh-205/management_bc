package ck4.nvb.rsmanagement.base.application.service;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import java.io.Serializable;

public interface CrudService<
        D extends EntityDto<ID>, T extends IEntity<ID>, ID extends Comparable<ID> & Serializable>
    extends GetService<D, T, ID> {

  /**
   * Create a new object
   *
   * @param createDto thr object to be created
   * @return the created object
   */
  D create(CreateInput<T> createDto) throws AppException;

  /**
   * Modify an object
   *
   * @param id the object identifier to be modified
   * @param updateDto the object to be modified
   * @return the updated object
   */
  D update(ID id, UpdateInput<T> updateDto) throws AppException;

  /**
   * Completely deletes an object from the database
   *
   * @param id the object identifier to be deleted
   */
  void delete(ID id) throws AppException;
}
