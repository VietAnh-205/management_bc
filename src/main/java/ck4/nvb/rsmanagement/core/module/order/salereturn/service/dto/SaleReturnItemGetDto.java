package ck4.nvb.rsmanagement.core.module.order.salereturn.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleReturnItemGetDto extends EntityDto<Long> {
  private Long saleReturnId;
  private String productName;
  private Long originalSaleLineId;
  private Integer qtyReturned;
  private Integer unitPriceAtSale;
  private Integer returnUnitPrice;
  private String conditionNote;
}
