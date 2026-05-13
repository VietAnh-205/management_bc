package ck4.nvb.rsmanagement.core.module.stores.store.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.stores.store.domain.Store;
import ck4.nvb.rsmanagement.core.module.stores.store.domain.StoreRepository;
import ck4.nvb.rsmanagement.core.module.stores.store.service.dto.StoreDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("storeService")
public class StoreCrudServiceImpl
    extends FullAuditedCrudServiceImpl<StoreDto, Store, Long, UserGetDto, Long> {

  protected StoreCrudServiceImpl(StoreRepository repository) {
    super(repository, Store.class);
  }

  @Override
  public StoreRepository getRepository() {
    return (StoreRepository) super.getRepository();
  }

  @Override
  public StoreDto mapToEntityDto(Store entity) {
    return new ModelMapper().map(entity, StoreDto.class);
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("name", List.of(SearchOperator.EQUALS, SearchOperator.CONTAINS));
    keys.put("address", List.of(SearchOperator.EQUALS, SearchOperator.CONTAINS));
    keys.put("phone", List.of(SearchOperator.EQUALS, SearchOperator.CONTAINS));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("name");
    return keys;
  }
}
