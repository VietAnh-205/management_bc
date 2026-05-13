package ck4.nvb.rsmanagement.core.module.stores.product.service.dto;

public interface TopProductProjection {
  Long getId();

  String getSku();

  String getName();

  Integer getUnitPrice();

  Long getTotalQuantity();
}
