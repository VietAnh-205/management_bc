package ck4.nvb.rsmanagement.base.application.service;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.PagedAndSortedResultRequestDto;
import ck4.nvb.rsmanagement.base.application.dto.PagedResultDto;
import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.base.application.exception.IllegalPropertyException;
import ck4.nvb.rsmanagement.base.application.utils.PredicateBuilder;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.domain.repository.BaseRepository;
import ck4.nvb.rsmanagement.base.domain.repository.OffsetBasedPageable;
import ck4.nvb.rsmanagement.base.web.error.FieldError;
import ck4.nvb.rsmanagement.base.web.utils.SearchCriteria;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@Getter
@Transactional(readOnly = true)
public abstract class GetServiceImpl<
        D extends EntityDto<ID>, T extends IEntity<ID>, ID extends Comparable<ID> & Serializable>
    implements GetService<D, T, ID> {

  @PersistenceContext private EntityManager entityManager;

  private final Class<T> type;

  private final BaseRepository<T, ID> repository;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected GetServiceImpl(BaseRepository<T, ID> repository, Class<T> type) {
    this.repository = repository;
    this.type = type;
  }

  public abstract D mapToEntityDto(T entity);

  public D mapToGetOutputDto(T entity) {
    return mapToEntityDto(entity);
  }

  public List<D> mapToGetListOutputDto(List<T> entities) {
    return entities.stream().map(this::mapToEntityDto).collect(Collectors.toList());
  }

  @Override
  public boolean exists(ID id) {
    return getRepository().existsById(id);
  }

  public T getEntity(ID id) {
    return getRepository().findById(id).orElse(null);
  }

  @Override
  public D get(ID id) throws AppException {
    T entity = getEntity(id);
    if (entity == null) {
      return null;
    }
    return mapToGetOutputDto(entity);
  }

  @Override
  public List<D> getAll() throws AppException {
    return mapToGetListOutputDto(getRepository().findAll());
  }

  public Set<String> getSortableKeys() {
    Set<String> keys = new HashSet<>();
    keys.add("id");
    return keys;
  }

  public Map<String, String> getReplaceKeyMap() {
    return new HashMap<>();
  }

  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = new HashMap<>();
    keys.put("id", List.of(SearchOperator.EQUALS));
    return keys;
  }

  @Override
  public long count(List<SearchCriteria> filter) throws AppException {
    return repository.count(mapToPredicate(filter));
  }

  @Override
  public List<D> getAll(List<SearchCriteria> filter) throws AppException {
    return mapToGetListOutputDto(
        StreamSupport.stream(getRepository().findAll(mapToPredicate(filter)).spliterator(), false)
            .collect(Collectors.toList()));
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResultDto<D> getPage(PagedAndSortedResultRequestDto paging) throws AppException {
    Page<T> result = getRepository().findAll(mapToPageable(paging));
    return new PagedResultDto<>(
        result.getTotalElements(), mapToGetListOutputDto(result.getContent()));
  }

  @Override
  public PagedResultDto<D> getPage(
      List<SearchCriteria> filter, PagedAndSortedResultRequestDto paging) throws AppException {
    Page<T> result = getRepository().findAll(mapToPredicate(filter), mapToPageable(paging));
    return new PagedResultDto<>(
        result.getTotalElements(), mapToGetListOutputDto(result.getContent()));
  }

  private OffsetBasedPageable mapToPageable(PagedAndSortedResultRequestDto paging) {
    if (getSortableKeys() == null
        || getSortableKeys().isEmpty()
        || paging.getSort() == null
        || paging.getSort().isEmpty()) {
      return new OffsetBasedPageable(
          Math.max(0, paging.getOffset()), Math.max(1, paging.getLimit()));
    }

    // Split and trim sort params ("+name,-createdTime" -> ["+name", "-createdTime"])
    String[] requestSorts =
        Arrays.stream(paging.getSort().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
    if (requestSorts.length == 0) {
      return new OffsetBasedPageable(
          Math.max(0, paging.getOffset()), Math.max(1, paging.getLimit()));
    }

    List<Sort.Order> sorts = new ArrayList<>();
    for (String requestSort : requestSorts) {
      String column = requestSort.trim();
      Sort.Direction direction = Sort.Direction.ASC;

      if (column.startsWith("-")) {
        direction = Sort.Direction.DESC;
        column = column.substring(1);
      } else if (column.startsWith("+")) {
        column = column.substring(1);
      }

      // Áp dụng replaceKeyMap nếu có
      String key = getReplaceKeyMap().getOrDefault(column, column);
      if (!getSortableKeys().contains(key)) continue;

      sorts.add(new Sort.Order(direction, key));
    }

    if (sorts.isEmpty()) {
      return new OffsetBasedPageable(
          Math.max(0, paging.getOffset()), Math.max(1, paging.getLimit()));
    }
    return new OffsetBasedPageable(
        Math.max(0, paging.getOffset()), Math.max(1, paging.getLimit()), Sort.by(sorts));
  }

  public Predicate mapToPredicate(List<SearchCriteria> filter) {
    if (filter == null || filter.isEmpty()) throw new AppException("Null search criteria");

    List<SearchCriteria> criteria = new ArrayList<>();
    List<FieldError> fieldErrors = new ArrayList<>();
    for (SearchCriteria c : filter) {
      if (!getSearchableKeys().containsKey(c.getKey())) {
        fieldErrors.add(new FieldError(c.getKey(), "Non searchable key"));
        continue;
      }
      if (!getSearchableKeys().get(c.getKey()).contains(c.getOperator())) {
        fieldErrors.add(
            new FieldError(c.getKey(), "Not supported operator " + c.getOperator().name()));
        continue;
      }

      criteria.add(c);
    }

    if (!fieldErrors.isEmpty()) throw new IllegalPropertyException(fieldErrors);
    logger.debug("Mapping to predicate with field: {}", filter);

    return new PredicateBuilder<>(type).and(criteria).replaceKeyMap(getReplaceKeyMap()).build();
  }
}
