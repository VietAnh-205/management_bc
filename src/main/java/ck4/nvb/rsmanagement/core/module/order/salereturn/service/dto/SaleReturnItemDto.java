package ck4.nvb.rsmanagement.core.module.order.salereturn.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.order.salereturn.domain.SaleReturnItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleReturnItemDto extends EntityDto<Long>
    implements CreateInput<SaleReturnItem>, UpdateInput<SaleReturnItem> {

  private Long saleReturnId;
  private Long productId;
  private Long originalSaleLineId;
  private Integer qtyReturned;
  private Integer unitPriceAtSale;
  private Integer returnUnitPrice;
  private String conditionNote;

  @Override
  public SaleReturnItem mapToEntity() {
    return new ModelMapper().map(this, SaleReturnItem.class);
  }

  @Override
  public boolean mapToEntity(SaleReturnItem entity) {
    return false;
  }
}
