package ck4.nvb.rsmanagement.base.application.service;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.PagedAndSortedResultRequestDto;
import ck4.nvb.rsmanagement.base.application.dto.PagedResultDto;
import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.CreationAudited;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.web.utils.SearchCriteria;
import java.io.Serializable;
import java.util.List;

public interface CreationAuditedGetService<
        D extends EntityDto<ID>,
        T extends IEntity<ID> & CreationAudited<UID>,
        ID extends Comparable<ID> & Serializable,
        U extends EntityDto<UID>,
        UID extends Comparable<UID> & Serializable>
    extends GetService<D, T, ID> {

  boolean exists(ID id, U user);

  D get(ID id, U user) throws AppException;

  List<D> getAll(List<SearchCriteria> filters, U user) throws AppException;

  PagedResultDto<D> getPage(PagedAndSortedResultRequestDto paging, U user) throws AppException;

  PagedResultDto<D> getPage(
      List<SearchCriteria> filter, PagedAndSortedResultRequestDto paging, U user)
      throws AppException;
}
