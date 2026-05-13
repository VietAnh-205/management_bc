package ck4.nvb.rsmanagement.core.module.stores.supplier.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.stores.supplier.domain.Supplier;
import ck4.nvb.rsmanagement.core.module.stores.supplier.domain.SupplierRepository;
import ck4.nvb.rsmanagement.core.module.stores.supplier.service.dto.SupplierDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("supplierService")
public class SupplierServiceImpl
    extends FullAuditedCrudServiceImpl<SupplierDto, Supplier, Long, UserGetDto, Long>
    implements ISupplierService {

  protected SupplierServiceImpl(SupplierRepository repository) {
    super(repository, Supplier.class);
  }

  @Override
  public SupplierRepository getRepository() {
    return (SupplierRepository) super.getRepository();
  }

  @Override
  public SupplierDto mapToEntityDto(Supplier entity) {
    return new ModelMapper().map(entity, SupplierDto.class);
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("name", List.of(SearchOperator.EQUALS, SearchOperator.CONTAINS));
    keys.put("location_id", List.of(SearchOperator.CONTAINS, SearchOperator.EQUALS));
    keys.put("contact", List.of(SearchOperator.CONTAINS));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("name");
    keys.add("location_id");
    keys.add("contact");
    return keys;
  }
}
