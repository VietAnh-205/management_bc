package ck4.nvb.rsmanagement.core.module.stores.batch.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchGetDto extends EntityDto<Long> {
  private String batchCode;
  private String supplierName;
  private Integer importedPrice;
  private Integer originalQty;
  private LocalDateTime manufactureDate;
  private LocalDateTime expiryDate;
}
