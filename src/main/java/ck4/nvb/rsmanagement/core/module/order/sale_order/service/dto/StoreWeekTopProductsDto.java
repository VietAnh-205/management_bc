package ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto;

import ck4.nvb.rsmanagement.core.module.stores.product.service.dto.ProductGetDto;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Data

public class StoreWeekTopProductsDto {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long storeId;
  private LocalDate weekStart;
  private LocalDate weekEnd;
  private List<ProductGetDto.WithSales> products;

  public StoreWeekTopProductsDto() {}

  public StoreWeekTopProductsDto(Long storeId, LocalDate weekStart, LocalDate weekEnd,
      List<ProductGetDto.WithSales> products) {
    this.storeId = storeId;
    this.weekStart = weekStart;
    this.weekEnd = weekEnd;
    this.products = products;
  }
}
