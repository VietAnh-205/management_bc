package ck4.nvb.rsmanagement.base.application.dto;

import ck4.nvb.rsmanagement.base.web.utils.SearchCriteria;
import java.util.List;

public interface FilterInput {

  PagedAndSortedResultRequestDto getPaging();

  void setPaging(PagedAndSortedResultRequestDto paging);

  List<SearchCriteria> mapToSearchCriteria();
}
