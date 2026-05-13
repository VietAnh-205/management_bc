package ck4.nvb.rsmanagement.core.module.stores.product.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductGetDto extends EntityDto<Long> {
  private String sku;
  private String name;
  private String description;
  private Integer unitPrice;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long categoryId;

  // Nested record cho specific use cases
  public record WithSales(
      Long id,
      String sku,
      String name,
      String description,
      Integer unitPrice,
      Long categoryId,
      Long totalQuantitySold) {
    // Constructor từ ProductGetDto
    public WithSales(ProductGetDto product, Long totalQuantitySold) {
      this(
          product.getId(),
          product.getSku(),
          product.getName(),
          product.getDescription(),
          product.getUnitPrice(),
          product.getCategoryId(),
          totalQuantitySold);
    }
  }
  public record WithStock(
      Long id, String sku, String name, Integer unitPrice, Integer remainingStock) {}
}
