package ck4.nvb.rsmanagement.base.application.service;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.PagedAndSortedResultRequestDto;
import ck4.nvb.rsmanagement.base.application.dto.PagedResultDto;
import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.web.utils.SearchCriteria;
import java.io.Serializable;
import java.util.List;

public interface GetService<
    D extends EntityDto<ID>, T extends IEntity<ID>, ID extends Comparable<ID> & Serializable> {

  /**
   * Check the existence of an object matching a given identifier
   *
   * @param id - the identifier
   * @return the existence value
   */
  boolean exists(ID id);

  /**
   * Obtains an object matching a given identifier
   *
   * @param id - the identifier
   * @return the matching object
   */
  D get(ID id) throws AppException;

  /**
   * Return a list of objects
   *
   * @return a list of objects of given class
   */
  List<D> getAll() throws AppException;

  long count(List<SearchCriteria> filter) throws AppException;

  /**
   * Return a list of objects
   *
   * @param filter thr search criteria
   * @return a list of objects of the given class
   */
  List<D> getAll(List<SearchCriteria> filter) throws AppException;

  /**
   * Return a page of objects
   *
   * @param paging the paging and sorting condition
   * @return a page of objects of the given class
   */
  PagedResultDto<D> getPage(PagedAndSortedResultRequestDto paging) throws AppException;

  /**
   * Return a page of objects
   *
   * @param filter the search criteria
   * @param paging the paging and sorting condition
   * @return a page of objects of the given class
   */
  PagedResultDto<D> getPage(List<SearchCriteria> filter, PagedAndSortedResultRequestDto paging)
      throws AppException;
}
