package ck4.nvb.rsmanagement.base.web.controller;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.FilterInput;
import ck4.nvb.rsmanagement.base.application.dto.PagedAndSortedResultRequestDto;
import ck4.nvb.rsmanagement.base.application.dto.PagedResultDto;
import ck4.nvb.rsmanagement.base.application.exception.ObjectNotFoundException;
import ck4.nvb.rsmanagement.base.application.service.CreationAuditedCrudService;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.CreationAudited;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.base.web.controller.api.PageResponse;
import ck4.nvb.rsmanagement.base.web.utils.SearchCriteria;
import ck4.nvb.rsmanagement.base.web.utils.SearchCriteriaParser;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Getter
public abstract class AuditedGetController<
    D extends EntityDto<ID>,
    T extends IEntity<ID> & CreationAudited<UID>,
    ID extends Comparable<ID> & Serializable,
    User extends EntityDto<UID>,
    UID extends Comparable<UID> & Serializable> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final CreationAuditedCrudService<D, T, ID, User, UID> service;

  protected AuditedGetController(CreationAuditedCrudService<D, T, ID, User, UID> service) {
    this.service = service;
  }

  public abstract User extractUser(Authentication auth);

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<D>>> getList(
      Authentication auth,
      @RequestParam(required = false, name = "query") List<String> query,
      @RequestParam(required = false, name = "sort") String sort,
      @RequestParam(required = false, name = "offset", defaultValue = "0") int offset,
      @RequestParam(required = false, name = "limit", defaultValue = "20") int limit) {

    User user = extractUser(auth);
    PagedResultDto<D> page;

    if (query != null) {
      List<SearchCriteria> params = SearchCriteriaParser.parse(query);
      if (params.isEmpty()) {
        page = getService().getPage(new PagedAndSortedResultRequestDto(offset, limit, sort), user);
      } else {
        page =
            getService()
                .getPage(params, new PagedAndSortedResultRequestDto(offset, limit, sort), user);
      }
    } else {
      page = getService().getPage(new PagedAndSortedResultRequestDto(offset, limit, sort), user);
    }

    PageResponse<D> pageResponse = buildPageResponse(page, offset, limit);
    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<D>> getById(Authentication auth, @PathVariable ID id) {
    User user = extractUser(auth);
    D output = getService().get(id, user);

    if (output == null) throw new ObjectNotFoundException("Object not found. Invalid ID: " + id);

    return ResponseEntity.ok(ApiResponse.success(output));
  }

  public ResponseEntity<ApiResponse<PageResponse<D>>> getList(
      Authentication auth, FilterInput request) {
    User user = extractUser(auth);
    if (request.getPaging() == null) request.setPaging(new PagedAndSortedResultRequestDto());

    PagedResultDto<D> page =
        getService().getPage(request.mapToSearchCriteria(), request.getPaging(), user);

    PageResponse<D> pageResponse =
        buildPageResponse(page, request.getPaging().getOffset(), request.getPaging().getLimit());
    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }

  @GetMapping("/count")
  public ResponseEntity<ApiResponse<Long>> count(
      @RequestParam(required = false, name = "query") List<String> query) {
    long count;
    if (query != null) {
      List<SearchCriteria> params = SearchCriteriaParser.parse(query);
      count = getService().count(params);
    } else {
      count = getService().count(null);
    }
    return ResponseEntity.ok(ApiResponse.success(count));
  }

  private PageResponse<D> buildPageResponse(PagedResultDto<D> page, int offset, int limit) {
    int currentPage = offset / limit;
    int totalPages = (int) Math.ceil((double) page.getTotalElements() / limit);

    return PageResponse.<D>builder()
        .content(page.getElements())
        .totalPages(totalPages)
        .totalElements(page.getTotalElements())
        .currentPage(currentPage)
        .pageSize(limit)
        .first(currentPage == 0)
        .last(currentPage >= totalPages - 1 || totalPages == 0)
        .empty(page.getElements().isEmpty())
        .build();
  }
}
