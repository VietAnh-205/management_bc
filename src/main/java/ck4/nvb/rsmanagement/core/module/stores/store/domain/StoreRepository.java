package ck4.nvb.rsmanagement.core.module.stores.store.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.stereotype.Repository;

@Repository("storeRepository")
public interface StoreRepository extends BaseFullAuditedRepository<Store, Long, Long> {}
