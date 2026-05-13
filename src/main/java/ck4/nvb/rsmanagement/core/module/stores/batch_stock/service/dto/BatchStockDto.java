package ck4.nvb.rsmanagement.core.module.stores.batch_stock.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStock;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class BatchStockDto extends EntityDto<Long>
    implements CreateInput<BatchStock>, UpdateInput<BatchStock> {

  @JsonSerialize(using = ToStringSerializer.class)
  private Long batchId;

  @JsonSerialize(using = ToStringSerializer.class)
  private Long storeId;

  private String status;
  private Integer version;
  private LocalDateTime etaAt;

  @Override
  public BatchStock mapToEntity() {
    return new ModelMapper().map(this, BatchStock.class);
  }

  @Override
  public boolean mapToEntity(BatchStock entity) {
    return false;
  }
}
