package ck4.nvb.rsmanagement.base.web.controller.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
  private List<T> content;
  private int totalPages;
  private long totalElements;
  private int currentPage;
  private int pageSize;
  private boolean first;
  private boolean last;
  private boolean empty;
}
