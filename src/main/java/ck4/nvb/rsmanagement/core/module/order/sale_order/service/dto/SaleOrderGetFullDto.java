package ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleOrderGetFullDto extends BaseSaleOrderDto {

  private String customerName;

  @JsonSerialize(using = ToStringSerializer.class)
  private Long storeId;

  private String voucherCode;

  private Integer finalPrice;

  private String paymentMethodName;

  private List<SaleLineGetDto> saleLines;
}
