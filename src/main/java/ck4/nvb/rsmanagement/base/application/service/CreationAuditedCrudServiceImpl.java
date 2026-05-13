package ck4.nvb.rsmanagement.base.application.service;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.base.application.exception.DuplicateIdentifierException;
import ck4.nvb.rsmanagement.base.application.exception.ObjectNotFoundException;
import ck4.nvb.rsmanagement.base.domain.entity.CreationAuditedGeneratedIdEntity;
import ck4.nvb.rsmanagement.base.domain.entity.CreationAuditedSerialIdEntity;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.CreationAudited;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.domain.repository.BaseRepository;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class CreationAuditedCrudServiceImpl<
        D extends EntityDto<ID>,
        T extends IEntity<ID> & CreationAudited<UID>,
        ID extends Comparable<ID> & Serializable,
        U extends EntityDto<UID>,
        UID extends Comparable<UID> & Serializable>
    extends CreationAuditedGetServiceImpl<D, T, ID, U, UID>
    implements CreationAuditedCrudService<D, T, ID, U, UID> {

  protected CreationAuditedCrudServiceImpl(BaseRepository<T, ID> repository, Class<T> type) {
    super(repository, type);
  }

  public void checkCreatePermission(CreateInput<T> createDto, U user) {
    getLogger().debug("CheckCreatePermission dto: {} user: {}", createDto, user);
  }

  public void checkUpdatePermission(ID id, U user) {
    getLogger().debug("CheckUpdatePermission id: {} user: {}", id, user);
  }

  public void checkDeletePermission(ID id, U user) {
    getLogger().debug("CheckDeletePermission id: {} user: {}", id, user);
  }

  public T mapToEntity(CreateInput<T> createDto, U user) {
    T entity = createDto.mapToEntity();
    entity.setCreatorId(user.getId());
    entity.setCreatedTime(LocalDateTime.now());
    entity.setNew(true);
    return entity;
  }

  protected D createEntity(T entity) {
    // check duplicated ID
    if (entity.getId() != null && exists(entity.getId())) {
      getLogger().error("Duplicate id {}", entity.getId());
      throw new DuplicateIdentifierException("Duplicate identifier " + entity.getId());
    }

    entity = getRepository().save(entity);
    getLogger()
        .info("Created entity id {} by user {}: {}", entity.getId(), entity.getCreatorId(), entity);

    return mapToEntityDto(entity);
  }

  @Override
  public D create(CreateInput<T> createDto, U user) throws AppException {
    // pre-check create permission
    checkCreatePermission(createDto, user);

    // convert to entity
    T entity = mapToEntity(createDto, user);

    // set null ID for auto-generated ID entities
    if (entity instanceof CreationAuditedSerialIdEntity
        || entity instanceof CreationAuditedGeneratedIdEntity) {
      entity.setId(null);
    }

    return createEntity(entity);
  }

  public boolean mapToEntity(UpdateInput<T> updateDto, T entity, U user) {
    return updateDto.mapToEntity(entity);
  }

  @Override
  public D update(ID id, UpdateInput<T> updateDto, U user) throws AppException {
    checkUpdatePermission(id, user);

    T entity = getEntity(id);
    if (entity == null) {
      throw new ObjectNotFoundException("Not found id " + id);
    }

    if (mapToEntity(updateDto, entity, user)) {
      entity = getRepository().save(entity);
      getLogger().info("Updated entity id {} by user {}: {}", id, entity.getCreatorId(), entity);
    }

    return mapToEntityDto(entity);
  }

  @Override
  public void delete(ID id, U user) throws AppException {
    checkDeletePermission(id, user);

    getRepository().deleteById(id);
    getLogger().info("Deleted entity id {} by user {}", id, user);
  }
}
