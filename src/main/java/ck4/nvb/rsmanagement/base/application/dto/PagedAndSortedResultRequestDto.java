package ck4.nvb.rsmanagement.base.application.dto;

public class PagedAndSortedResultRequestDto extends PagedResultRequestDto {

  private String sort;

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  public PagedAndSortedResultRequestDto() {
    super();
  }

  public PagedAndSortedResultRequestDto(String sort) {
    super();
    this.sort = sort;
  }

  public PagedAndSortedResultRequestDto(int offset, int limit, String sort) {
    super(offset, limit);
    this.sort = sort;
  }
}
