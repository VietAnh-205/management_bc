package ck4.nvb.rsmanagement.base.application.dto;

import java.util.List;

public class PagedResultDto<T extends Dto> extends ListResultDto<T> implements HasTotalCount {
  private long totalElements = 0;

  public long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(long totalElements) {
    this.totalElements = totalElements;
  }

  public PagedResultDto(long totalElements, List<T> elements) {
    super(elements);
    this.totalElements = totalElements;
  }
}
