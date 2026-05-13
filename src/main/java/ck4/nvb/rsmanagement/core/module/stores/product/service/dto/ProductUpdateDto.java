package ck4.nvb.rsmanagement.core.module.stores.product.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateDto extends BaseProductDto implements UpdateInput<Product> {

  private String sku;
  private String name;
  private String description;
  private Integer unitPrice;
  private Long categoryId;
  private boolean enabled;

  @Override
  public boolean mapToEntity(Product entity) {
    boolean isModified = false;

    if (sku == null || !sku.equals(entity.getSku())) {
      entity.setSku(sku);
      isModified = true;
    }

    if (name == null || !name.equals(entity.getName())) {
      entity.setName(name);
      isModified = true;
    }
    if (description == null || !description.equals(entity.getDescription())) {
      entity.setDescription(description);
      isModified = true;
    }

    if (Double.compare(unitPrice, entity.getUnitPrice()) != 0) {
      entity.setUnitPrice(unitPrice);
      isModified = true;
    }

    if (entity.getCategoryId() == null || (long) categoryId != entity.getCategoryId()) {
      entity.setCategoryId(categoryId);
      isModified = true;
    }
    return isModified;
  }
}
