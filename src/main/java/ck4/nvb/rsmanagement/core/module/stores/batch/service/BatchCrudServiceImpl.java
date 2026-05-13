package ck4.nvb.rsmanagement.core.module.stores.batch.service;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.Batch;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchItem;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchItemRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.BatchRepository;
import ck4.nvb.rsmanagement.core.module.stores.batch.service.dto.BatchDto;
import ck4.nvb.rsmanagement.core.module.stores.batch.service.dto.BatchItemCreateDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("batchService")
public class BatchCrudServiceImpl
    extends FullAuditedCrudServiceImpl<BatchDto, Batch, Long, UserGetDto, Long>
    implements IBatchService {

  protected BatchCrudServiceImpl(BatchRepository repository) {
    super(repository, Batch.class);
  }

  @Autowired private ModelMapper modelMapper;

  @Autowired private BatchItemRepository batchItemRepository;

  @Override
  public BatchRepository getRepository() {
    return (BatchRepository) super.getRepository();
  }

  @Override
  public BatchDto mapToEntityDto(Batch entity) {
    return modelMapper.map(entity, BatchDto.class);
  }

  // getSearchableKeys / getSortableKeys giữ nguyên như trước (nếu cần)
  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put(
        "quantity",
        List.of(
            SearchOperator.BETWEEN,
            SearchOperator.LESS_THAN,
            SearchOperator.GREATER_THAN,
            SearchOperator.GREATER_THAN_OR_EQUAL,
            SearchOperator.LESS_THAN_OR_EQUAL));
    keys.put(
        "importedPrice",
        List.of(
            SearchOperator.BETWEEN,
            SearchOperator.LESS_THAN,
            SearchOperator.GREATER_THAN,
            SearchOperator.GREATER_THAN_OR_EQUAL,
            SearchOperator.LESS_THAN_OR_EQUAL));
    keys.put(
        "manufacturingDate",
        List.of(
            SearchOperator.BETWEEN,
            SearchOperator.GREATER_THAN_OR_EQUAL,
            SearchOperator.LESS_THAN_OR_EQUAL,
            SearchOperator.EQUALS));
    keys.put(
        "expiryDate",
        List.of(
            SearchOperator.EQUALS,
            SearchOperator.GREATER_THAN_OR_EQUAL,
            SearchOperator.LESS_THAN_OR_EQUAL,
            SearchOperator.BETWEEN));
    keys.put(
        "currentQuantity",
        List.of(
            SearchOperator.BETWEEN,
            SearchOperator.GREATER_THAN_OR_EQUAL,
            SearchOperator.LESS_THAN_OR_EQUAL));
    keys.put("status", List.of(SearchOperator.EQUALS));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("quantity");
    keys.add("importedPrice");
    keys.add("manufacturingDate");
    keys.add("expiryDate");
    keys.add("currentQuantity");
    keys.add("status");
    return keys;
  }

  @Override
  public List<BatchDto> findByProductId(Long productId) {
    List<Batch> batches = getRepository().findByProductId(productId);
    return batches.stream().map(b -> toDto(b, productId)).collect(Collectors.toList());
  }

  @Override
  public List<BatchDto> findByProductIds(List<Long> productIds) {
    if (productIds == null || productIds.isEmpty()) {
      return List.of();
    }
    List<Batch> batches = getRepository().findByProductIdIn(productIds);
    // map với productId đầu tiên (giữ lại logic cũ)
    return batches.stream().map(b -> toDto(b, productIds.get(0))).collect(Collectors.toList());
  }

  private BatchDto toDto(Batch b, Long productId) {
    BatchDto dto = new BatchDto();
    BeanUtils.copyProperties(b, dto);
    // Lấy các batch_item của batch
    List<BatchItem> items =
        batchItemRepository.findByBatchId(b.getId()).stream()
            .filter(bi -> productId == null || productId.equals(bi.getProductId()))
            .collect(Collectors.toList());
    if (!items.isEmpty()) {
      BatchItem bi = items.get(0);
      dto.setImportedPrice(bi.getImportPrice());
      dto.setOriginalQty(bi.getOriginalQty());
      dto.setSupplierId(bi.getSupplierId());
      dto.setManufactureDate(bi.getManufactureDate());
      dto.setExpiryDate(bi.getExpiryDate());
    }
    return dto;
  }

  @Override
  @Transactional
  public BatchDto create(CreateInput<Batch> input, UserGetDto user) {
    if (input instanceof BatchDto) {
      BatchDto batchDto = (BatchDto) input;
      BatchDto created = super.create(input, user);

      if (batchDto.getItems() != null && !batchDto.getItems().isEmpty()) {
        List<BatchItem> toSave = new ArrayList<>();
        for (BatchItemCreateDto it : batchDto.getItems()) {
          BatchItem bi = new BatchItem();
          bi.setBatchId(created.getId());
          bi.setProductId(it.getProductId());
          bi.setSupplierId(it.getSupplierId());
          bi.setOriginalQty(it.getQty());
          bi.setImportPrice(it.getImportPrice());
          bi.setManufactureDate(it.getManufactureDate());
          bi.setExpiryDate(it.getExpiryDate());
          toSave.add(bi);
        }
        batchItemRepository.saveAll(toSave);
      }

      return created;
    }

    return super.create(input, user);
  }
}
