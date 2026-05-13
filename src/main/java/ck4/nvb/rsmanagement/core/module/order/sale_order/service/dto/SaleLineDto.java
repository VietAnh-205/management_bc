package ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleLine;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
@NoArgsConstructor
public class SaleLineDto extends EntityDto<Long>
        implements CreateInput<SaleLine>, UpdateInput<SaleLine> {

  private String saleOrderId;
  private Long productId;
  private Integer qtyOrdered;
  private Integer qtyAllocated;
  private Integer qtyPicked;
  private Integer unitPrice;

  @Override
  public SaleLine mapToEntity() {
    return new ModelMapper().map(this, SaleLine.class);
  }

  @Override
  public boolean mapToEntity(SaleLine entity) {
    boolean isModified = false;

    if (entity == null) {
      return false;
    }
    if (!saleOrderId.equals(entity.getSaleOrderId())) {
      entity.setSaleOrderId(saleOrderId);
      isModified = true;
    }
    if (!productId.equals(entity.getProductId())) {
      entity.setProductId(productId);
      isModified = true;
    }
    if (!qtyOrdered.equals(entity.getQtyOrdered())) {
      entity.setQtyOrdered(qtyOrdered);
      isModified = true;
    }
    if (!qtyAllocated.equals(entity.getQtyAllocated())) {
      entity.setQtyAllocated(qtyAllocated);
      isModified = true;
    }
    if (!qtyPicked.equals(entity.getQtyPicked())) {
      entity.setQtyPicked(qtyPicked);
      isModified = true;
    }
    return isModified;
  }
}