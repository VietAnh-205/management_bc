package ck4.nvb.rsmanagement.core.module.stores.batch_stock.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStock;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.domain.BatchStockRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.service.dto.BatchStockDto;
import ck4.nvb.rsmanagement.core.module.stores.batch_stock.service.dto.BatchStockGetDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service("importLogService")
public class BatchStockServiceImpl
    extends FullAuditedCrudServiceImpl<BatchStockDto, BatchStock, Long, UserGetDto, Long> {

  protected BatchStockServiceImpl(BatchStockRepository repository) {
    super(repository, BatchStock.class);
  }

  @Override
  public BatchStockRepository getRepository() {
    return (BatchStockRepository) super.getRepository();
  }

  @Override
  public BatchStockDto mapToEntityDto(BatchStock entity) {
    return new ModelMapper().map(entity, BatchStockDto.class);
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("fromStock", List.of(SearchOperator.EQUALS));
    keys.put("toStore", List.of(SearchOperator.EQUALS));
    keys.put(
        "startDate",
        List.of(
            SearchOperator.EQUALS,
            SearchOperator.GREATER_THAN,
            SearchOperator.LESS_THAN,
            SearchOperator.BETWEEN));
    keys.put(
        "deliveryDate",
        List.of(
            SearchOperator.EQUALS,
            SearchOperator.LESS_THAN,
            SearchOperator.GREATER_THAN,
            SearchOperator.BETWEEN));
    keys.put("status", List.of(SearchOperator.EQUALS));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("startDate");
    keys.add("deliveryDate");
    return keys;
  }

  public List<BatchStockGetDto> getAvailableBatchInfoByProductAndStore(
      Long productId, Long storeId) {
    return getRepository().findAvailableBatchInfoByProductAndStore(productId, storeId);
  }

  public Long getTotalAvailableQuantityByProductAndStore(Long productId, Long storeId) {
    Long t = getRepository().getTotalAvailableQuantityByProductAndStore(productId, storeId);
    return t == null ? 0L : t;
  }
}
