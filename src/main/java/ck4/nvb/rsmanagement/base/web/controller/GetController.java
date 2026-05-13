package ck4.nvb.rsmanagement.base.web.controller;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.FilterInput;
import ck4.nvb.rsmanagement.base.application.dto.PagedAndSortedResultRequestDto;
import ck4.nvb.rsmanagement.base.application.dto.PagedResultDto;
import ck4.nvb.rsmanagement.base.application.exception.ObjectNotFoundException;
import ck4.nvb.rsmanagement.base.application.service.GetService;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import ck4.nvb.rsmanagement.base.web.controller.api.PageResponse;
import ck4.nvb.rsmanagement.base.web.utils.SearchCriteria;
import ck4.nvb.rsmanagement.base.web.utils.SearchCriteriaParser;
import java.io.Serializable;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public class GetController<
    D extends EntityDto<ID>, T extends IEntity<ID>, ID extends Comparable<ID> & Serializable> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public Logger getLogger() {
    return logger;
  }

  private final GetService<D, T, ID> service;

  protected GetController(GetService<D, T, ID> service) {
    this.service = service;
  }

  public GetService<D, T, ID> getService() {
    return service;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<D>>> getList(
      @RequestParam(required = false, name = "query") List<String> query,
      @RequestParam(required = false, name = "sort") String sort,
      @RequestParam(required = false, name = "offset", defaultValue = "0") int offset,
      @RequestParam(required = false, name = "limit", defaultValue = "20") int limit) {

    PagedAndSortedResultRequestDto paging = new PagedAndSortedResultRequestDto(offset, limit, sort);
    PagedResultDto<D> page;

    if (query != null) {
      List<SearchCriteria> params = SearchCriteriaParser.parse(query);
      page = params.isEmpty() ? getService().getPage(paging) : getService().getPage(params, paging);
    } else {
      page = getService().getPage(paging);
    }

    PageResponse<D> pageResponse = buildPageResponse(page, offset, limit);
    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<D>> getById(@PathVariable ID id) {
    D output = getService().get(id);
    if (output == null) {
      throw new ObjectNotFoundException("Object not found. Invalid ID: " + id);
    }
    return ResponseEntity.ok(ApiResponse.success(output));
  }

  public ResponseEntity<ApiResponse<PageResponse<D>>> getList(FilterInput request) {
    if (request.getPaging() == null) {
      request.setPaging(new PagedAndSortedResultRequestDto());
    }
    PagedResultDto<D> page =
        getService().getPage(request.mapToSearchCriteria(), request.getPaging());

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
