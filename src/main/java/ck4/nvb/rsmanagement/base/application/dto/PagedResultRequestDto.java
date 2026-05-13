package ck4.nvb.rsmanagement.base.application.dto;

public class PagedResultRequestDto extends LimitedResultRequestDto {

  private int offset;

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public PagedResultRequestDto() {
    super();
    offset = 0;
  }

  public PagedResultRequestDto(int offset) {
    super();
    this.offset = offset;
  }

  public PagedResultRequestDto(int offset, int limit) {
    super(limit);
    this.offset = offset;
  }
}
