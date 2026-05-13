package ck4.nvb.rsmanagement.core.module.order.paymentmethod.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain.PaymentMethod;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.service.dto.PaymentMethodDto;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.service.dto.PaymentMethodGetDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;

public interface IPaymentService
    extends FullAuditedCrudService<PaymentMethodDto, PaymentMethod, Long, UserGetDto, Long> {
  List<PaymentMethodGetDto.WithUsageStats> getUsageStatsOfInterval(int days);
}
