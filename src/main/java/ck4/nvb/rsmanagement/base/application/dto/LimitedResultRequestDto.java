package ck4.nvb.rsmanagement.base.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LimitedResultRequestDto extends Dto {

  public static int DEFAULT_MAX_RESULT_COUNT = 10;

  private int limit;

  public LimitedResultRequestDto() {
    limit = DEFAULT_MAX_RESULT_COUNT;
  }

  public LimitedResultRequestDto(int limit) {
    this.limit = limit;
  }
}
