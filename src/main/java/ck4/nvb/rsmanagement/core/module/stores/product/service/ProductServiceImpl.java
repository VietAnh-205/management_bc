package ck4.nvb.rsmanagement.core.module.stores.product.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.base.web.utils.SearchOperator;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.Product;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.ProductRepository;
import ck4.nvb.rsmanagement.core.module.stores.product.service.dto.ProductGetDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("productService")
public class ProductServiceImpl
        extends FullAuditedCrudServiceImpl<ProductGetDto, Product, Long, UserGetDto, Long>
        implements IProductService {

  protected ProductServiceImpl(ProductRepository repository) {
    super(repository, Product.class);
  }

  @Autowired private ModelMapper modelMapper;

  @Override
  public ProductGetDto mapToEntityDto(Product entity) {
    return modelMapper.map(entity, ProductGetDto.class);
  }

  @Override
  public ProductRepository getRepository() {
    return (ProductRepository) super.getRepository();
  }

  @Override
  public Map<String, List<SearchOperator>> getSearchableKeys() {
    Map<String, List<SearchOperator>> keys = super.getSearchableKeys();
    keys.put("sku", List.of(SearchOperator.CONTAINS, SearchOperator.EQUALS));
    keys.put("name", List.of(SearchOperator.CONTAINS, SearchOperator.EQUALS));
    return keys;
  }

  @Override
  public Set<String> getSortableKeys() {
    Set<String> keys = super.getSortableKeys();
    keys.add("sku");
    keys.add("name");
    keys.add("categoryId");
    keys.add("unitPrice");
    return keys;
  }

  /**
   * @param productId productId of a product
   * @param storeId storeId of a store
   * @return qty of a product of a store
   */
  @Override
  public int getRemainQuantity(long productId, long storeId) {
    return getRepository().remainQuantity(productId, storeId);
  }

  // MỚI: trả về tất cả product của một store (đã map thành DTO)
  @Transactional(readOnly = true)
  public List<ProductGetDto> getAllProductsOfAStore(Long storeId) {
    List<Product> products = getRepository().findAllByStoreViaBatches(storeId);
    return products.stream().map(this::mapToEntityDto).collect(Collectors.toList());
  }

  // TÙY CHỌN: nhẹ hơn nếu bạn chỉ cần ids (dùng trong coldProducts để filter)
  @Transactional(readOnly = true)
  public List<Long> getAllProductIdsOfAStore(Long storeId) {
    return getRepository().findProductIdsByStoreViaBatches(storeId);
  }
}
