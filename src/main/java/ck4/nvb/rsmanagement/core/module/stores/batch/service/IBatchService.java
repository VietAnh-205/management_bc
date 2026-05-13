package ck4.nvb.rsmanagement.core.module.stores.batch.service;

import ck4.nvb.rsmanagement.base.application.service.FullAuditedCrudService;
import ck4.nvb.rsmanagement.core.module.stores.batch.domain.Batch;
import ck4.nvb.rsmanagement.core.module.stores.batch.service.dto.BatchDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.List;

public interface IBatchService
    extends FullAuditedCrudService<BatchDto, Batch, Long, UserGetDto, Long> {
  List<BatchDto> findByProductId(Long productId);

  List<BatchDto> findByProductIds(List<Long> productIds);
}
