package ck4.nvb.rsmanagement.core.module.stores.location.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.stores.location.domain.Location;
import java.math.BigDecimal;

public class LocationDto extends EntityDto<Long>
    implements CreateInput<ck4.nvb.rsmanagement.core.module.stores.location.domain.Location>,
        UpdateInput<ck4.nvb.rsmanagement.core.module.stores.location.domain.Location> {

  private BigDecimal latitude;
  private BigDecimal longitude;

  @Override
  public ck4.nvb.rsmanagement.core.module.stores.location.domain.Location mapToEntity() {
    Location location = new Location();
    location.setLatitude(latitude);
    location.setLongitude(longitude);
    return location;
  }

  @Override
  public boolean mapToEntity(Location entity) {
    boolean isModified = false;

    if (!(latitude == entity.getLatitude())) {
      entity.setLatitude(latitude);
      isModified = true;
    }

    if (!(longitude == entity.getLongitude())) {
      entity.setLongitude(longitude);
      isModified = true;
    }

    return isModified;
  }
}
