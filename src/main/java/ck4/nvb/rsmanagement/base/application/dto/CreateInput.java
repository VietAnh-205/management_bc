package ck4.nvb.rsmanagement.base.application.dto;

import java.io.Serializable;

public interface CreateInput<T extends Serializable> {

  T mapToEntity();
}
