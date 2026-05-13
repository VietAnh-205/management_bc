package ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SaleLineGetDto extends EntityDto<Long> {
  private String saleOrderId;

  @JsonSerialize(using = ToStringSerializer.class)
  private Long productId;

  private String productName;
  private Integer qtyOrdered;
  private Integer unitPrice;
  private Long totalPrice;
}
