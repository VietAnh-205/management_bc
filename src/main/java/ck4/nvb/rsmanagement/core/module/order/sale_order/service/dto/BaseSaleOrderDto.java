package ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseSaleOrderDto extends EntityDto<String> {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long customerId;

  private String note;
}
