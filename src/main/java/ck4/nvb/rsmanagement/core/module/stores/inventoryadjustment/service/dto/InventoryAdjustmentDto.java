package ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.domain.InventoryAdjustment;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class InventoryAdjustmentDto extends EntityDto<Long>
    implements CreateInput<InventoryAdjustment>, UpdateInput<InventoryAdjustment> {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long batchStockId;

  private Integer changeQuantity;

  private String reason;

  @Override
  public InventoryAdjustment mapToEntity() {
    return new ModelMapper().map(this, InventoryAdjustment.class);
  }

  @Override
  public boolean mapToEntity(InventoryAdjustment entity) {
    boolean isModified = false;
    if (!Objects.equals(batchStockId, entity.getBatchStockId())) {
      entity.setBatchStockId(batchStockId);
      isModified = true;
    }
    if (!Objects.equals(changeQuantity, entity.getChangeQuantity())) {
      entity.setChangeQuantity(changeQuantity);
      isModified = true;
    }
    if (!Objects.equals(reason, entity.getReason())) {
      entity.setReason(reason);
      isModified = true;
    }
    return isModified;
  }
}
