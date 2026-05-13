package ck4.nvb.rsmanagement.core.module.stores.category.domain;

import ck4.nvb.rsmanagement.base.domain.repository.BaseFullAuditedRepository;
import org.springframework.stereotype.Repository;

@Repository("categoryRepository")
public interface CategoryRepository extends BaseFullAuditedRepository<Category, Long, Long> {}
