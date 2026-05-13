package ck4.nvb.rsmanagement.core.module.order.sale_order.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudServiceImpl;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleAllocation;
import ck4.nvb.rsmanagement.core.module.order.sale_order.domain.SaleAllocationRepository;
import ck4.nvb.rsmanagement.core.module.order.sale_order.service.dto.SaleAllocationDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("saleAllocationService")
public class SaleAllocationServiceImpl
    extends FullAuditedCrudServiceImpl<SaleAllocationDto, SaleAllocation, Long, UserGetDto, Long>
    implements ISaleAllocationService {

  protected SaleAllocationServiceImpl(SaleAllocationRepository repository) {
    super(repository, SaleAllocation.class);
  }

  @Autowired private ModelMapper modelMapper;

  @Override
  public SaleAllocationRepository getRepository() {
    return (SaleAllocationRepository) super.getRepository();
  }

  @Override
  public SaleAllocationDto mapToEntityDto(SaleAllocation entity) {
    return modelMapper.map(entity, SaleAllocationDto.class);
  }

  @Override
  public Integer getTotalSoldQuantityByBatchItem(Long batchItemId) {
    if (batchItemId == null) return 0;
    Integer total = getRepository().sumSoldQtyByBatchItemId(batchItemId);
    return total == null ? 0 : total;
  }

  @Override
  public Integer getTotalSoldQuantityByBatchStockAndProduct(Long batchStockId, Long productId) {
    if (batchStockId == null || productId == null) return 0;
    Integer total = getRepository().sumSoldQtyByBatchStockAndProduct(batchStockId, productId);
    return total == null ? 0 : total;
  }
}
