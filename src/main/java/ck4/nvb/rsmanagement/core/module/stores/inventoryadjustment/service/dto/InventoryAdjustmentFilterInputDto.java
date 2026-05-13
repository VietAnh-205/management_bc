package ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.FilterInput;
import ck4.nvb.rsmanagement.base.application.dto.PagedAndSortedResultRequestDto;
import ck4.nvb.rsmanagement.base.web.utils.SearchCriteria;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryAdjustmentFilterInputDto implements FilterInput {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long batchStockId;

  private PagedAndSortedResultRequestDto paging = new PagedAndSortedResultRequestDto();

  @Override
  public List<SearchCriteria> mapToSearchCriteria() {
    List<SearchCriteria> criteria = new ArrayList<>();
    if (batchStockId != null) {
      criteria.add(
          new SearchCriteria("batchStockId", SearchOperator.EQUALS, batchStockId.toString()));
    }
    return criteria;
  }
}
