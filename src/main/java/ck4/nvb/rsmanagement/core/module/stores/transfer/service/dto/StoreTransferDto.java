package ck4.nvb.rsmanagement.core.module.stores.transfer.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.stores.transfer.domain.StoreTransfer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
public class StoreTransferDto extends EntityDto<Long>
    implements CreateInput<StoreTransfer>, UpdateInput<StoreTransfer> {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long fromStoreId;

  @JsonSerialize(using = ToStringSerializer.class)
  private Long toStoreId;

  List<StoreTransferItemDto> items;
  private LocalDateTime transferDate;
  private String status;

  @Override
  public StoreTransfer mapToEntity() {
    StoreTransfer storeTransfer = new StoreTransfer();
    storeTransfer.setFromStoreId(fromStoreId);
    storeTransfer.setToStoreId(toStoreId);
    storeTransfer.setTransferDate(transferDate);
    storeTransfer.setStatus(status);
    return storeTransfer;
  }

  @Override
  public boolean mapToEntity(StoreTransfer entity) {
    return false;
  }
}
