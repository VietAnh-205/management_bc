package ck4.nvb.rsmanagement.core.module.order.voucher.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.order.voucher.domain.VoucherCustomer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherCustomerDto extends EntityDto<Long>
    implements CreateInput<VoucherCustomer>, UpdateInput<VoucherCustomer> {
  private Long voucherId;
  private Long customerId;
  private Boolean issued;

  @Override
  public VoucherCustomer mapToEntity() {
    return new ModelMapper().map(this, VoucherCustomer.class);
  }

  @Override
  public boolean mapToEntity(VoucherCustomer entity) {
    return false;
  }
}
