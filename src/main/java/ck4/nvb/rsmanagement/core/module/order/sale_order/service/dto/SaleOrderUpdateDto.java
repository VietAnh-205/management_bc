package ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleOrderUpdateDto extends BaseSaleOrderDto implements UpdateInput<SaleOrder> {

  @JsonSerialize(using = ToStringSerializer.class)
  private Long storeId;

  @NotEmpty private List<SaleLineDto> lines;

  @JsonSerialize(using = ToStringSerializer.class)
  private Long voucherId;

  private Long paymentId;

  @Override
  public boolean mapToEntity(SaleOrder entity) {
    boolean isModified = false;
    if (entity == null) {
      return false;
    }
    if (!getCustomerId().equals(entity.getCustomerId())) {
      entity.setCustomerId(getCustomerId());
      isModified = true;
    }
    if (!storeId.equals(entity.getStoreId())) {
      entity.setStoreId(storeId);
      isModified = true;
    }
    if (!voucherId.equals(entity.getVoucherId())) {
      entity.setVoucherId(voucherId);
      isModified = true;
    }
    if (!getNote().equals(entity.getNote())) {
      entity.setNote(getNote());
      isModified = true;
    }
    if (!paymentId.equals(entity.getPaymentId())) {
      entity.setPaymentId(paymentId);
      isModified = true;
    }

    return isModified;
  }
}
