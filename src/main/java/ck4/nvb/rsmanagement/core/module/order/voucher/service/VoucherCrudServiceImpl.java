package ck4.nvb.rsmanagement.core.module.order.voucher.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.order.voucher.domain.Voucher;
import ck4.nvb.rsmanagement.core.module.order.voucher.domain.VoucherRepository;
import ck4.nvb.rsmanagement.core.module.order.voucher.service.dto.VoucherDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("voucherService")
public class VoucherCrudServiceImpl
    extends FullAuditedCrudServiceImpl<VoucherDto, Voucher, Long, UserGetDto, Long> {
  protected VoucherCrudServiceImpl(VoucherRepository repository) {
    super(repository, Voucher.class);
  }

  @Override
  public VoucherRepository getRepository() {
    return (VoucherRepository) super.getRepository();
  }

  @Override
  public VoucherDto mapToEntityDto(Voucher entity) {
    return new ModelMapper().map(entity, VoucherDto.class);
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("code", List.of(SearchOperator.EQUALS));
    keys.put(
        "discountPercent",
        List.of(
            SearchOperator.EQUALS,
            SearchOperator.GREATER_THAN,
            SearchOperator.BETWEEN,
            SearchOperator.LESS_THAN,
            SearchOperator.LESS_THAN_OR_EQUAL,
            SearchOperator.GREATER_THAN_OR_EQUAL));
    keys.put(
        "discountValue",
        List.of(
            SearchOperator.EQUALS,
            SearchOperator.LESS_THAN,
            SearchOperator.BETWEEN,
            SearchOperator.GREATER_THAN,
            SearchOperator.LESS_THAN_OR_EQUAL,
            SearchOperator.GREATER_THAN_OR_EQUAL));
    keys.put("startTime", List.of(SearchOperator.BETWEEN));
    keys.put("expirationTime", List.of(SearchOperator.BETWEEN));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("code");
    keys.add("discountPercent");
    keys.add("discountValue");
    return keys;
  }
}
