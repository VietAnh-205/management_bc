package ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.domain.InventoryAdjustment;
import ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.domain.InventoryAdjustmentRepository;
import ck4.nvb.rsmanagement.core.module.stores.inventoryadjustment.service.dto.InventoryAdjustmentDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("storeStockService")
public class InventoryAdjustmentServiceImpl
    extends FullAuditedCrudServiceImpl<
        InventoryAdjustmentDto, InventoryAdjustment, Long, UserGetDto, Long> {

  protected InventoryAdjustmentServiceImpl(InventoryAdjustmentRepository repository) {
    super(repository, InventoryAdjustment.class);
  }

  @Override
  public InventoryAdjustmentRepository getRepository() {
    return (InventoryAdjustmentRepository) super.getRepository();
  }

  @Override
  public InventoryAdjustmentDto mapToEntityDto(InventoryAdjustment entity) {
    return new ModelMapper().map(entity, InventoryAdjustmentDto.class);
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("batchStockId", List.of(SearchOperator.EQUALS, SearchOperator.CONTAINS));
    keys.put("reason", List.of(SearchOperator.EQUALS));
    keys.put(
        "changeQuantity",
        List.of(
            SearchOperator.BETWEEN,
            SearchOperator.LESS_THAN,
            SearchOperator.GREATER_THAN,
            SearchOperator.GREATER_THAN_OR_EQUAL,
            SearchOperator.LESS_THAN_OR_EQUAL));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("batchStockId");
    keys.add("changeQuantity");
    keys.add("reason");
    return keys;
  }
}
