package ck4.nvb.rsmanagement.core.module.order.voucher.service.dto;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.core.module.order.voucher.domain.Voucher;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
@NoArgsConstructor
public class VoucherDto extends EntityDto<Long>
    implements CreateInput<Voucher>, UpdateInput<Voucher> {
  private String code;
  private String desc;
  private Integer discountPer;
  private Integer discountVal;
  private LocalDateTime validFrom;
  private LocalDateTime validTo;
  private Long qtyTotal;
  private Long qtyRedeemed;
  private Integer perCustomerLimit;
  private String audienceType;

  @Override
  public Voucher mapToEntity() {
    return new ModelMapper().map(this, Voucher.class);
  }

  @Override
  public boolean mapToEntity(Voucher entity) {
    boolean isModified = false;
    if (entity == null) {
      return false;
    }
    if (!code.equals(entity.getCode())) {
      entity.setCode(code);
      isModified = true;
    }
    if (!desc.equals(entity.getDesc())) {
      entity.setDesc(desc);
      isModified = true;
    }
    if (!discountPer.equals(entity.getDiscountPer())) {
      entity.setDiscountPer(discountPer);
      isModified = true;
    }
    if (!discountVal.equals(entity.getDiscountVal())) {
      entity.setDiscountVal(discountVal);
      isModified = true;
    }
    // haven't add time modify yet (sua sau)

    return isModified;
  }
}
