package ck4.nvb.rsmanagement.core.module.stores.store.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.stores.store.domain.Store;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class StoreDto extends EntityDto<Long> implements CreateInput<Store>, UpdateInput<Store> {

  private String name;

  private String address;

  @JsonSerialize(using = ToStringSerializer.class)
  private Long locationId;

  private String phone;

  @Override
  public Store mapToEntity() {
    return new ModelMapper().map(this, Store.class);
  }

  @Override
  public boolean mapToEntity(Store entity) {
    boolean isModified = false;

    if (entity.getName() != null) {
      this.name = entity.getName();
      isModified = true;
    }

    if (entity.getAddress() != null) {
      this.address = entity.getAddress();
      isModified = true;
    }

    if (entity.getLocationId() != null) {
      this.locationId = entity.getLocationId();
    }

    if (entity.getPhone() != null) {
      this.phone = entity.getPhone();
      isModified = true;
    }
    return isModified;
  }
}
