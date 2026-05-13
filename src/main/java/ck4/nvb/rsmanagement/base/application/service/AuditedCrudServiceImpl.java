package ck4.nvb.rsmanagement.base.application.service;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.Audited;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.domain.repository.BaseRepository;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class AuditedCrudServiceImpl<
        D extends EntityDto<ID>,
        T extends IEntity<ID> & Audited<UID>,
        ID extends Comparable<ID> & Serializable,
        U extends EntityDto<UID>,
        UID extends Comparable<UID> & Serializable>
    extends CreationAuditedCrudServiceImpl<D, T, ID, U, UID>
    implements AuditedCrudService<D, T, ID, U, UID> {

  protected AuditedCrudServiceImpl(BaseRepository<T, ID> repository, Class<T> type) {
    super(repository, type);
  }

  @Override
  public T mapToEntity(CreateInput<T> createDto, U user) {
    T entity = super.mapToEntity(createDto, user);
    entity.setUpdaterID(user.getId());
    entity.setUpdatedTime(entity.getCreatedTime());
    return entity;
  }

  @Override
  public boolean mapToEntity(UpdateInput<T> updateDto, T entity, U user) {
    if (!super.mapToEntity(updateDto, entity, user)) {
      return false;
    }

    entity.setUpdaterID(user.getId());
    entity.setUpdatedTime(LocalDateTime.now());
    return true;
  }
}
