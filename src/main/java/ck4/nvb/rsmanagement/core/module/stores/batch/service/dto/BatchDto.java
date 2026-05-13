package ck4.nvb.rsmanagement.core.module.stores.batch.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.Batch;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class BatchDto extends EntityDto<Long> implements CreateInput<Batch>, UpdateInput<Batch> {
  private String batchCode;

  private Long supplierId;
  private Integer importedPrice;
  private Integer originalQty;
  private LocalDateTime manufactureDate;
  private LocalDateTime expiryDate;
  private LocalDateTime arrivalDate;
  private List<BatchItemCreateDto> items;

  @Override
  public Batch mapToEntity() {
    return new ModelMapper().map(this, Batch.class);
  }

  @Override
  public boolean mapToEntity(Batch entity) {
    return false;
  }
}
