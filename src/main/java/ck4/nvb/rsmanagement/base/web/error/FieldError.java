package ck4.nvb.rsmanagement.base.web.error;

import java.io.Serializable;

public class FieldError implements Serializable {
  private String field;
  private String description;

  public FieldError() {}
  ;

  public FieldError(String field) {
    this.field = field;
  }

  public FieldError(String field, String description) {
    this.field = field;
    this.description = description;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return field + ": " + description;
  }
}
