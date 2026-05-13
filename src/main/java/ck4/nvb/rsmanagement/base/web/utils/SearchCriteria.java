package ck4.nvb.rsmanagement.base.web.utils;

import ck4.nvb.rsmanagement.base.application.dto.Dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchCriteria extends Dto {
  private String key;
  private SearchOperator operator;
  private String value;

  public SearchCriteria(final String key, final SearchOperator operator, final String value) {
    super();
    this.key = key;
    this.operator = operator;
    this.value = value;
  }
}
