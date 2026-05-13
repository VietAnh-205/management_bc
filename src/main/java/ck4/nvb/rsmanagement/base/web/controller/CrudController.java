package ck4.nvb.rsmanagement.base.web.controller;

import ck4.nvb.rsmanagement.base.application.dto.CreateInput;
import ck4.nvb.rsmanagement.base.application.dto.EntityDto;
import ck4.nvb.rsmanagement.base.application.dto.UpdateInput;
import ck4.nvb.rsmanagement.base.application.exception.ObjectNotFoundException;
import ck4.nvb.rsmanagement.base.application.service.CrudService;
import ck4.nvb.rsmanagement.base.domain.entity.interfaces.IEntity;
import ck4.nvb.rsmanagement.base.web.controller.api.ApiResponse;
import java.io.Serializable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public class CrudController<
        D extends EntityDto<ID>,
        T extends IEntity<ID>,
        ID extends Comparable<ID> & Serializable,
        C extends CreateInput<T>,
        U extends UpdateInput<T>>
    extends GetController<D, T, ID> {

  protected CrudController(CrudService<D, T, ID> service) {
    super(service);
  }

  public CrudService<D, T, ID> getService() {
    return (CrudService<D, T, ID>) super.getService();
  }

  @PostMapping
  public ResponseEntity<ApiResponse<D>> create(@RequestBody C entity) {
    D result = getService().create(entity);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.<D>builder()
                .code(201)
                .message("Created successfully!")
                .data(result)
                .build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<D>> update(@PathVariable ID id, @RequestBody U entity) {
    D result = getService().update(id, entity);
    return ResponseEntity.ok(
        ApiResponse.<D>builder().code(200).message("Updated successfully!").data(result).build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable ID id) {
    if (!getService().exists(id))
      throw new ObjectNotFoundException("Object not found. Invalid ID: " + id);

    getService().delete(id);
    return ResponseEntity.ok(
        ApiResponse.<Void>builder().code(200).message("Deleted successfully!").data(null).build());
  }
}
