package ck4.nvb.rsmanagement.core.module.order.voucher.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.order.voucher.domain.VoucherRedemption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherRedemptionDto extends EntityDto<Long>
    implements CreateInput<VoucherRedemption>, UpdateInput<VoucherRedemption> {
  private Long voucherId;
  private Long customerId;
  private String saleOrderId;
  private Integer appliedValue;

  @Override
  public VoucherRedemption mapToEntity() {
    return new ModelMapper().map(this, VoucherRedemption.class);
  }

  @Override
  public boolean mapToEntity(VoucherRedemption entity) {
    return false;
  }
}
