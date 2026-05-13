package ck4.nvb.rsmanagement.core.module.order.voucher.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("voucherRepository")
public interface VoucherRepository extends BaseFullAuditedRepository<Voucher, Long, Long> {
  Voucher findFirstByCodeAndDeletedFalse(String voucherCode);

  @Modifying
  @Query(
      value =
          "UPDATE voucher v SET v.qty_redeemed = v.qty_redeemed + 1 WHERE v.id = :id AND (v.qty_total IS NULL OR v.qty_redeemed < v.qty_total)",
      nativeQuery = true)
  int incrementQtyRedeemedIfAvailable(@Param("id") Long id);

  // optionally add findWithLock
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT v FROM Voucher v WHERE v.id = :id")
  Voucher findByIdForUpdate(@Param("id") Long id);
}
