package ck4.nvb.rsmanagement.core.module.order.paymentmethod.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentMethodGetDto extends EntityDto<Long> {
  private String code;
  private String name;

  public record WithUsageStats(String name, Integer usage) {
    public WithUsageStats(PaymentMethodGetDto paymentMethod, Integer usage) {
      this(paymentMethod.getName(), usage);
    }
  }
}
