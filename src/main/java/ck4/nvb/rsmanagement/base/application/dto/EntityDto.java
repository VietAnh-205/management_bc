package ck4.nvb.rsmanagement.base.application.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class EntityDto<ID extends Comparable<ID> & Serializable> extends Dto {

  @Serial private static final long serialVersionUID = 1L;

  @JsonSerialize(using = ToStringSerializer.class)
  private ID id;
}
