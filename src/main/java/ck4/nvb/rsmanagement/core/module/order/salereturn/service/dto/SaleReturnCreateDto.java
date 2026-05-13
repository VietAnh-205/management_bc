package ck4.nvb.rsmanagement.core.module.order.salereturn.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.core.module.order.salereturn.domain.SaleReturn;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleReturnCreateDto extends EntityDto<Long> implements CreateInput<SaleReturn> {
  private String returnCode;
  private String originalSaleOrderId;
  private Long storeId;
  private Long customerId;
  List<SaleReturnItemDto> items;
  private String returnReason;
  private Integer totalReturnAmount;
  private String refundMethod;
  private Boolean isProcessed;
  private LocalDateTime processedAt;
  private Long processedBy;

  @Override
  public SaleReturn mapToEntity() {
    return null;
  }
}
