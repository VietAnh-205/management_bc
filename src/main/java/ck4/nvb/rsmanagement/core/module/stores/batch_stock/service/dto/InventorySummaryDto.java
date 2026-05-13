package ck4.nvb.rsmanagement.core.module.stores.batch_stock.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummaryDto {
  private Long productId;
  private String productName;
  private Integer totalQuantity;
  private Integer availableQuantity;
  private Integer reservedQuantity;
}
