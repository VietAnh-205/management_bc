package ck4.nvb.rsmanagement.core.module.stores.supplier.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.stores.supplier.domain.Supplier;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierDto extends EntityDto<Long>
    implements CreateInput<Supplier>, UpdateInput<Supplier> {

  private String name;

  private String address;

  @JsonSerialize(using = ToStringSerializer.class)
  private Long locationId;

  private String contact;

  @Override
  public Supplier mapToEntity() {
    Supplier supplier = new Supplier();
    supplier.setName(name);
    supplier.setAddress(address);
    supplier.setLocationId(locationId);
    supplier.setContact(contact);
    return supplier;
  }

  @Override
  public boolean mapToEntity(Supplier entity) {
    boolean isModified = false;

    if (entity.getId() == null) {
      return false;
    }

    if (!name.equals(entity.getName())) {
      entity.setName(name);
      isModified = true;
    }

    if (!address.equals(entity.getAddress())) {
      entity.setAddress(address);
    }

    if (!locationId.equals(entity.getLocationId())) {
      entity.setLocationId(locationId);
      isModified = true;
    }

    if (!contact.equals(entity.getContact())) {
      entity.setContact(contact);
      isModified = true;
    }
    return isModified;
  }
}
