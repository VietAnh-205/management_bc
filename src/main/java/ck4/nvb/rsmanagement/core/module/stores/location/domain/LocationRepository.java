package ck4.nvb.rsmanagement.core.module.stores.location.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.stereotype.Repository;

@Repository("locationRepository")
public interface LocationRepository extends BaseFullAuditedRepository<Location, Long, Long> {}
