package ck4.nvb.rsmanagement.core.module.stores.product.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.Product;
import ck4.nvb.rsmanagement.core.module.stores.product.service.dto.ProductGetDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;

import java.util.List;

public interface IProductService
    extends FullAuditedCrudService<ProductGetDto, Product, Long, UserGetDto, Long> {
  int getRemainQuantity(long productId, long storeId);
  List<ProductGetDto> getAllProductsOfAStore(Long storeId);
  List<Long> getAllProductIdsOfAStore(Long storeId);
}
