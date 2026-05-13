package ck4.nvb.rsmanagement.base.application.dto;

import ck4.nvb.rsmanagement.base.util.JacksonParser;
import java.io.Serial;
import java.io.Serializable;

public abstract class Dto implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  public String toString() {
    return super.getClass().getSimpleName() + " " + JacksonParser.getInstance().toJson(this);
  }
}
