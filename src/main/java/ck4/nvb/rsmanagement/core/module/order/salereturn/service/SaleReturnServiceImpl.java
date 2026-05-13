package ck4.nvb.rsmanagement.core.module.order.salereturn.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.order.salereturn.domain.SaleReturn;
import ck4.nvb.rsmanagement.core.module.order.salereturn.domain.SaleReturnRepository;
import ck4.nvb.rsmanagement.core.module.order.salereturn.service.dto.SaleReturnGetDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("saleReturnService")
public class SaleReturnServiceImpl
    extends FullAuditedCrudServiceImpl<SaleReturnGetDto, SaleReturn, Long, UserGetDto, Long>
    implements ISaleReturnService {

  @Autowired private ISaleReturnItemService saleReturnItemService;
  @Autowired private ModelMapper modelMapper;

  protected SaleReturnServiceImpl(SaleReturnRepository repository) {
    super(repository, SaleReturn.class);
  }

  @Override
  public SaleReturnRepository getRepository() {
    return (SaleReturnRepository) super.getRepository();
  }

  @Override
  public SaleReturnGetDto mapToEntityDto(SaleReturn entity) {
    return modelMapper.map(entity, SaleReturnGetDto.class);
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("return_code", List.of(SearchOperator.EQUALS));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("processed_at");
    return keys;
  }
}
