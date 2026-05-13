package ck4.nvb.rsmanagement.base.application.service;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.base.application.exception.AppException;
import ck4.nvb.rsmanagement.base.application.exception.DuplicateIdentifierException;
import ck4.nvb.rsmanagement.base.application.exception.ObjectNotFoundException;
import ck4.nvb.rsmanagement.base.domain.entity.GeneratedIdEntity;
import ck4.nvb.rsmanagement.base.domain.entity.SerialIdEntity;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.domain.repository.BaseRepository;
import java.io.Serializable;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class CrudServiceImpl<
        D extends EntityDto<ID>, T extends IEntity<ID>, ID extends Comparable<ID> & Serializable>
    extends GetServiceImpl<D, T, ID> implements CrudService<D, T, ID> {
  protected CrudServiceImpl(BaseRepository<T, ID> repository, Class<T> type) {
    super(repository, type);
  }

  public T mapToEntity(CreateInput<T> dto) {
    T entity = dto.mapToEntity();
    entity.setNew(true);
    return entity;
  }

  protected D createEntity(T entity) {
    // check duplicated ID
    if (entity.getId() != null && exists(entity.getId())) {
      throw new DuplicateIdentifierException("Duplicate identifier " + entity.getId());
    }

    entity = getRepository().save(entity);
    getLogger().info("Created entity {}", entity);
    return mapToEntityDto(entity);
  }

  @Override
  public D create(CreateInput<T> createDto) throws AppException {
    T entity = mapToEntity(createDto);

    // set null ID fo auto-generated ID entities
    if (entity instanceof SerialIdEntity || entity instanceof GeneratedIdEntity) {
      entity.setId(null);
    }
    return createEntity(entity);
  }

  public boolean mapToEntity(UpdateInput<T> dto, T entity) {
    return dto.mapToEntity(entity);
  }

  @Override
  public D update(ID id, UpdateInput<T> updateDto) throws AppException {
    T entity = getEntity(id);
    if (entity == null) throw new ObjectNotFoundException("Not found id " + id);

    if (mapToEntity(updateDto, entity)) {
      entity = getRepository().save(entity);
      getLogger().info("Updated entity {}", entity);
    }
    return mapToEntityDto(entity);
  }

  @Override
  public void delete(ID id) throws AppException {
    getRepository().deleteById(id);
    getLogger().info("Deleted entity with id {}", id);
  }
}
