package ck4.nvb.rsmanagement.core.module.stores.product.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductCreateDto extends BaseProductDto implements CreateInput<Product> {

  private String sku;
  private String name;
  private String description;
  private Integer unitPrice;
  private Long categoryId;

  @Override
  public Product mapToEntity() {
    Product product = new Product();
    product.setSku(sku);
    product.setName(name);
    product.setDescription(description);
    product.setUnitPrice(unitPrice);
    product.setCategoryId(categoryId);
    return product;
  }
}
