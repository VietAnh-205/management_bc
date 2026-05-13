package ck4.nvb.rsmanagement.base.application.service;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.CreationAudited;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import java.io.Serializable;

public interface CreationAuditedCrudService<
        D extends EntityDto<ID>,
        T extends IEntity<ID> & CreationAudited<UID>,
        ID extends Comparable<ID> & Serializable,
        U extends EntityDto<UID>,
        UID extends Comparable<UID> & Serializable>
    extends CreationAuditedGetService<D, T, ID, U, UID> {

  /**
   * Create a new object
   *
   * @param createDto the object to be created
   * @param user the user creates the object
   * @return the created object
   */
  D create(CreateInput<T> createDto, U user) throws AppException;

  /**
   * Modify an object
   *
   * @param id the object identifier to be modified
   * @param updateDto the object to be modified
   * @param user the user updates the object
   * @return the updated object
   */
  D update(ID id, UpdateInput<T> updateDto, U user) throws AppException;

  /**
   * Completely deletes an object from the database
   *
   * @param id the object identifier to be deleted
   * @param user the user deletes the object
   */
  void delete(ID id, U user) throws AppException;
}
