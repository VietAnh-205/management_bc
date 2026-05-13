package ck4.nvb.rsmanagement.base.domain.repository;

import ck4.nvb.rsmanagement.base.domain.entity.interfaces.FullAudited;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import java.io.Serializable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseFullAuditedRepository<
        T extends IEntity<ID> & FullAudited<UID>,
        ID extends Comparable<ID> & Serializable,
        UID extends Comparable<UID> & Serializable>
    extends BaseRepository<T, ID> {
  boolean existsByIdAndDeletedIsFalse(ID id);

  T findFirstByIdAndDeletedIsFalse(ID id);
}
