package ck4.nvb.rsmanagement.base.application.exception;

import ck4.nvb.rsmanagement.base.web.error.FieldError;
import java.io.Serial;
import java.util.List;

public class MissingRequiredPropertyException extends AppException {

  @Serial private static final long serialVersionUID = 1L;

  public MissingRequiredPropertyException() {}

  public MissingRequiredPropertyException(List<FieldError> fieldErrors) {
    super(fieldErrors);
  }

  public MissingRequiredPropertyException(String message) {
    super(message);
  }

  public MissingRequiredPropertyException(List<FieldError> fieldErrors, String message) {
    super(fieldErrors, message);
  }

  public MissingRequiredPropertyException(String message, Object... arguments) {
    super(message, arguments);
  }

  public MissingRequiredPropertyException(
      List<FieldError> fieldErrors, String message, Object... arguments) {
    super(fieldErrors, message, arguments);
  }

  public MissingRequiredPropertyException(String message, Throwable cause) {
    super(message, cause);
  }

  public MissingRequiredPropertyException(
      List<FieldError> fieldErrors, String message, Throwable cause) {
    super(fieldErrors, message, cause);
  }

  public MissingRequiredPropertyException(Throwable cause) {
    super(cause);
  }

  public MissingRequiredPropertyException(List<FieldError> fieldErrors, Throwable cause) {
    super(fieldErrors, cause);
  }
}
