package ck4.nvb.rsmanagement.base.web.controller;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.base.application.exception.ObjectNotFoundException;
import ck4.nvb.rsmanagement.base.application.service.CreationAuditedCrudService;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.CreationAudited;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import java.io.Serializable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

public abstract class AuditedCrudController<
        D extends EntityDto<ID>,
        T extends IEntity<ID> & CreationAudited<UID>,
        ID extends Comparable<ID> & Serializable,
        User extends EntityDto<UID>,
        UID extends Comparable<UID> & Serializable,
        C extends CreateInput<T>,
        U extends UpdateInput<T>>
    extends AuditedGetController<D, T, ID, User, UID> {

  protected AuditedCrudController(CreationAuditedCrudService<D, T, ID, User, UID> service) {
    super(service);
  }

  @PostMapping
  public ResponseEntity<ApiResponse<D>> create(Authentication auth, @RequestBody C entity) {
    User user = extractUser(auth);
    D result = getService().create(entity, user);
    return ResponseEntity.ok(
        ApiResponse.<D>builder().code(201).message("Created successfully!").data(result).build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<D>> update(
      Authentication auth, @PathVariable ID id, @RequestBody U entity) {
    User user = extractUser(auth);
    D result = getService().update(id, entity, user);
    return ResponseEntity.ok(
        ApiResponse.<D>builder().code(200).message("Updated successfully!").data(result).build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(Authentication auth, @PathVariable ID id) {
    User user = extractUser(auth);
    if (!getService().exists(id, user))
      throw new ObjectNotFoundException("Object not found. Invalid ID: " + id);

    getService().delete(id, user);
    return ResponseEntity.ok(
        ApiResponse.<Void>builder().code(200).message("Deleted successfully!").data(null).build());
  }
}
