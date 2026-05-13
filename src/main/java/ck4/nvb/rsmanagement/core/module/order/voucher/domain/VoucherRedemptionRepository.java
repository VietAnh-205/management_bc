package ck4.nvb.rsmanagement.core.module.order.voucher.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRedemptionRepository
    extends BaseFullAuditedRepository<VoucherRedemption, Long, Long> {
  @Query(
      "SELECT vr FROM VoucherRedemption vr WHERE vr.voucherId = :voucherId AND vr.saleOrderId IS NULL ORDER BY vr.createdTime ASC")
  VoucherRedemption findFirstByVoucherIdAndSaleOrderIdIsNull(@Param("voucherId") Long voucherId);
}
