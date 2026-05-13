package ck4.nvb.rsmanagement.core.module.stores.product.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.FilterInput;
import ck4.nvb.rsmanagement.base.application.dto.PagedAndSortedResultRequestDto;
import ck4.nvb.rsmanagement.base.web.utils.SearchCriteria;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductFilterInputDto implements FilterInput {

  // fillable fields for products
  private String name;
  private String description;
  private Integer minPrice;
  private Integer maxPrice;
  private Long categoryId;

  // paging and sorting
  private PagedAndSortedResultRequestDto paging;

  @Override
  public List<SearchCriteria> mapToSearchCriteria() {
    List<SearchCriteria> criteria = new ArrayList<>();

    if (name != null && !name.trim().isEmpty()) {
      criteria.add(new SearchCriteria("name", SearchOperator.CONTAINS, name));
    }

    if (description != null && !description.trim().isEmpty()) {
      criteria.add(new SearchCriteria("description", SearchOperator.CONTAINS, description));
    }

    if (minPrice != null) {
      criteria.add(
          new SearchCriteria(
              "unitPrice", SearchOperator.GREATER_THAN_OR_EQUAL, minPrice.toString()));
    }

    if (maxPrice != null) {
      criteria.add(
          new SearchCriteria("unitPrice", SearchOperator.LESS_THAN_OR_EQUAL, maxPrice.toString()));
    }

    if (categoryId != null) {
      criteria.add(new SearchCriteria("categoryId", SearchOperator.EQUALS, categoryId.toString()));
    }

    return criteria;
  }
}
