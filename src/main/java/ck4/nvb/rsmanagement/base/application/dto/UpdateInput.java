package ck4.nvb.rsmanagement.base.application.dto;

import java.io.Serializable;

public interface UpdateInput<T extends Serializable> {
  boolean mapToEntity(T entity);
}
