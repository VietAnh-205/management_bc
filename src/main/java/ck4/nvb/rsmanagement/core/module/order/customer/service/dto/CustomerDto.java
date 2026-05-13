package ck4.nvb.rsmanagement.core.module.order.customer.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.order.customer.domain.Customer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
@NoArgsConstructor
public class CustomerDto extends EntityDto<Long>
    implements CreateInput<Customer>, UpdateInput<Customer> {
  private String name;
  private String phone;
  private String gender;
  private Integer point;

  @Override
  public Customer mapToEntity() {
    return new ModelMapper().map(this, Customer.class);
  }

  @Override
  public boolean mapToEntity(Customer entity) {
    boolean isModified = false;
    if (entity == null) {
      return false;
    }
    if (!name.equals(entity.getName())) {
      entity.setName(name);
      isModified = true;
    }
    if (!phone.equals(entity.getPhone())) {
      entity.setPhone(phone);
      isModified = true;
    }
    if (!gender.equals(entity.getGender())) {
      entity.setGender(gender);
      isModified = true;
    }
    if (point != entity.getPoint()) {
      entity.setPoint(point);
      isModified = true;
    }
    return isModified;
  }
}
