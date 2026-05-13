package ck4.nvb.rsmanagement.core.module.stores.batch_stock.service.dto;

public interface BatchStockGetDto {
  String getProductName();

  Integer getQtyTotal();

  Integer getQtyAvailable();

  Integer getQtyReversed();

  String getBatchCode();

  String getSupplierName();

  Integer getImportedPrice();

  java.time.LocalDateTime getManufactureDate();

  java.time.LocalDateTime getExpiryDate();
}
