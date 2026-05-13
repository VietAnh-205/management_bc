package ck4.nvb.rsmanagement.core.module.order.report;

public class TopProductDto {
  public Long productId;
  public String sku;
  public String name;
  public Long qty;
  public Long revenue;

  public TopProductDto(Long productId, String sku, String name, Long qty, Long revenue) {
    this.productId = productId;
    this.sku = sku;
    this.name = name;
    this.qty = qty;
    this.revenue = revenue;
  }
}
