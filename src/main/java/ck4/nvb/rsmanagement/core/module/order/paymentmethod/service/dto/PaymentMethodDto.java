package ck4.nvb.rsmanagement.core.module.order.paymentmethod.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
@NoArgsConstructor
public class PaymentMethodDto extends EntityDto<Long>
    implements CreateInput<PaymentMethod>, UpdateInput<PaymentMethod> {
  private String code;
  private String name;

  @Override
  public PaymentMethod mapToEntity() {
    return new ModelMapper().map(this, PaymentMethod.class);
  }

  @Override
  public boolean mapToEntity(PaymentMethod entity) {
    return false;
  }
}
