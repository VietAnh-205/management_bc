package ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleAllocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleAllocationDto extends EntityDto<Long>
    implements CreateInput<SaleAllocation>, UpdateInput<SaleAllocation> {
  private Long saleLineId;
  private Long batchItemId;
  private Long batchStockId;
  private Integer soldQty;
  private Integer unitCostSnap;

  @Override
  public SaleAllocation mapToEntity() {
    return new ModelMapper().map(this, SaleAllocation.class);
  }

  @Override
  public boolean mapToEntity(SaleAllocation entity) {
    return false;
  }
}
