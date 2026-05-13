package ck4.nvb.rsmanagement.base.application.service;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.PagedAndSortedResultRequestDto;
import ck4.nvb.rsmanagement.base.application.dto.PagedResultDto;
import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.base.application.exception.ObjectNotFoundException;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.FullAudited;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import ck4.nvb.rsmanagement.base.domain.repository.BaseRepository;
import ck4.nvb.rsmanagement.base.web.utils.SearchCriteria;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class FullAuditedCrudServiceImpl<
        D extends EntityDto<ID>,
        T extends IEntity<ID> & FullAudited<UID>,
        ID extends Comparable<ID> & Serializable,
        U extends EntityDto<UID>,
        UID extends Comparable<UID> & Serializable>
    extends AuditedCrudServiceImpl<D, T, ID, U, UID>
    implements FullAuditedCrudService<D, T, ID, U, UID> {

  protected FullAuditedCrudServiceImpl(BaseRepository<T, ID> repository, Class<T> type) {
    super(repository, type);
  }

  @Override
  public BaseFullAuditedRepository<T, ID, UID> getRepository() {
    return (BaseFullAuditedRepository<T, ID, UID>) super.getRepository();
  }

  @Override
  public boolean exists(ID id) {
    return getRepository().existsByIdAndDeletedIsFalse(id);
  }

  @Override
  public T getEntity(ID id) {
    return getRepository().findFirstByIdAndDeletedIsFalse(id);
  }

  @Override
  public void delete(ID id, U user) throws AppException {
    checkDeletePermission(id, user);

    T entity = getEntity(id);
    if (entity == null) {
      throw new ObjectNotFoundException("Not found id: " + id);
    }

    entity.setDeleted(true);
    entity.setDeletedTime(LocalDateTime.now());
    entity.setDeleterID(user.getId());

    getRepository().save(entity);
    getLogger().info("Deleted entity id {} by user {}", id, user.getId());
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("deleted", List.of(SearchOperator.EQUALS));
    return keys;
  }

  public List<SearchCriteria> addDeletedFalse(List<SearchCriteria> filter) {
    // always create a new mutable list to avoid UnsupportedOperationException
    List<SearchCriteria> mutableFilter =
        filter == null ? new ArrayList<>() : new ArrayList<>(filter);

    boolean hasDeleted =
        mutableFilter.stream()
            .anyMatch(
                c -> "deleted".equals(c.getKey()) && SearchOperator.EQUALS.equals(c.getOperator()));

    if (!hasDeleted) {
      mutableFilter.add(
          new SearchCriteria("deleted", SearchOperator.EQUALS, Boolean.FALSE.toString()));
    }

    return mutableFilter;
  }

  @Override
  public List<D> getAll() throws AppException {
    return super.getAll(addDeletedFalse(null));
  }

  @Override
  public long count(List<SearchCriteria> filter) throws AppException {
    return super.count(addDeletedFalse(filter));
  }

  @Override
  public List<D> getAll(List<SearchCriteria> filter) throws AppException {
    return super.getAll(addDeletedFalse(filter));
  }

  @Override
  public T mapToEntity(CreateInput<T> createDto, U user) {
    return super.mapToEntity(createDto, user);
  }

  @Override
  public List<D> getAll(List<SearchCriteria> filters, U user) throws AppException {
    return super.getAll(addDeletedFalse(filters), user);
  }

  @Override
  public PagedResultDto<D> getPage(PagedAndSortedResultRequestDto paging, U user)
      throws AppException {
    return super.getPage(addDeletedFalse(null), paging, user);
  }

  @Override
  public PagedResultDto<D> getPage(
      List<SearchCriteria> filter, PagedAndSortedResultRequestDto paging, U user)
      throws AppException {
    return super.getPage(addDeletedFalse(filter), paging, user);
  }

  @Override
  public PagedResultDto<D> getPage(PagedAndSortedResultRequestDto paging) throws AppException {
    return super.getPage(addDeletedFalse(null), paging);
  }

  @Override
  public PagedResultDto<D> getPage(
      List<SearchCriteria> filter, PagedAndSortedResultRequestDto paging) throws AppException {
    return super.getPage(addDeletedFalse(filter), paging);
  }
}
