package ck4.nvb.rsmanagement.core.module.order.salereturn.domain;

import ck4.nvb.rsmanagement.base.domain.entity.FullAuditedSerialIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_return_item")
public class SaleReturnItem extends FullAuditedSerialIdEntity {
  @Column(name = "sale_return_id")
  private Long saleReturnId;

  @Column(name = "product_id")
  private Long productId;

  @Column(name = "original_sale_line_id")
  private Long originalSaleLineId;

  @Column(name = "qty_returned")
  private Integer qtyReturned;

  @Column(name = "unit_price_at_sale")
  private Integer unitPriceAtSale;

  @Column(name = "return_unit_price")
  private Integer returnUnitPrice;

  @Column(name = "condition_note")
  private String conditionNote;
}
