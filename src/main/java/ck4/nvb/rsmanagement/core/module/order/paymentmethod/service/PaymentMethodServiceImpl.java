package ck4.nvb.rsmanagement.core.module.order.paymentmethod.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain.PaymentMethod;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.domain.PaymentMethodRepository;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.service.dto.PaymentMethodDto;
import ck4.nvb.rsmanagement.core.module.order.paymentmethod.service.dto.PaymentMethodGetDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("paymentMethodService")
public class PaymentMethodServiceImpl
    extends FullAuditedCrudServiceImpl<PaymentMethodDto, PaymentMethod, Long, UserGetDto, Long>
    implements IPaymentService {

  protected PaymentMethodServiceImpl(PaymentMethodRepository paymentMethodRepository) {
    super(paymentMethodRepository, PaymentMethod.class);
  }

  @Override
  public PaymentMethodRepository getRepository() {
    return (PaymentMethodRepository) super.getRepository();
  }

  @Override
  public PaymentMethodDto mapToEntityDto(PaymentMethod entity) {
    return new ModelMapper().map(entity, PaymentMethodDto.class);
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("code", List.of(SearchOperator.EQUALS, SearchOperator.CONTAINS));
    keys.put("name", List.of(SearchOperator.EQUALS, SearchOperator.CONTAINS));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("name");
    keys.add("code");
    return keys;
  }

  @Override
  public List<PaymentMethodGetDto.WithUsageStats> getUsageStatsOfInterval(int days) {
    LocalDateTime end = LocalDateTime.now();
    LocalDateTime start = end.minusDays(days);
    List<Map<String, Object>> results = getRepository().findPaymentMethodUsageStats(start, end);
    return results.stream()
        .map(
            row ->
                new PaymentMethodGetDto.WithUsageStats(
                    (String) row.get("name"), ((Number) row.get("paymentUsage")).intValue()))
        .toList();
  }
}
