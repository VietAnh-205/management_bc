package ck4.nvb.rsmanagement.core.module.stores.product.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("productRepository")
public interface ProductRepository extends BaseFullAuditedRepository<Product, Long, Long> {
  @Query(
          value =
                  """
                    SELECT qty_available
                    FROM batch_stock
                    WHERE product_id = :productId
                      AND store_id = :storeId
                      AND deleted = false;
                """,
          nativeQuery = true)
  int remainQuantity(Long productId, Long storeId);

  int countProductsByDeletedIsFalse();

  // MỚI: trả về tất cả product (entity) đã được phân bổ cho store thông qua batch/batch_item
  @Query(value = """
    SELECT DISTINCT p.*
    FROM product p
    JOIN batch_item bi ON bi.product_id = p.id AND bi.deleted = false
    JOIN batch_stock bs ON bs.batch_id = bi.batch_id AND bs.deleted = false
    WHERE bs.store_id = :storeId
      AND p.deleted = false
    """, nativeQuery = true)
  List<Product> findAllByStoreViaBatches(Long storeId);

  // (Tùy chọn) chỉ trả về product ids — nhẹ hơn khi client chỉ cần id
  @Query(value = """
    SELECT DISTINCT p.id
    FROM product p
    JOIN batch_item bi ON bi.product_id = p.id AND bi.deleted = false
    JOIN batch_stock bs ON bs.batch_id = bi.batch_id AND bs.deleted = false
    WHERE bs.store_id = :storeId
      AND p.deleted = false
    """, nativeQuery = true)
  List<Long> findProductIdsByStoreViaBatches(Long storeId);
}
