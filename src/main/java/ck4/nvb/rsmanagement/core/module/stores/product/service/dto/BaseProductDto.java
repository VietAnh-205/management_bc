package ck4.nvb.rsmanagement.core.module.stores.product.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseProductDto extends EntityDto<Long> {

  private String sku;
  private String name;
  private String description;
  private Integer unitPrice;
  private Long categoryId;
}
