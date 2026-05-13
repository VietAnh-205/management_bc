package ck4.nvb.rsmanagement.base.application.exception;

import ck4.nvb.rsmanagement.base.web.error.FieldError;
import java.io.Serial;
import java.util.List;

public class IllegalPropertyException extends AppException {

  @Serial private static final long serialVersionUID = 1L;

  public IllegalPropertyException() {}

  public IllegalPropertyException(List<FieldError> fieldErrors) {
    super(fieldErrors);
  }

  public IllegalPropertyException(String message) {
    super(message);
  }

  public IllegalPropertyException(List<FieldError> fieldErrors, String message) {
    super(fieldErrors, message);
  }

  public IllegalPropertyException(String message, Object... arguments) {
    super(message, arguments);
  }

  public IllegalPropertyException(
      List<FieldError> fieldErrors, String message, Object... arguments) {
    super(fieldErrors, message, arguments);
  }

  public IllegalPropertyException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalPropertyException(List<FieldError> fieldErrors, String message, Throwable cause) {
    super(fieldErrors, message, cause);
  }

  public IllegalPropertyException(Throwable cause) {
    super(cause);
  }

  public IllegalPropertyException(List<FieldError> fieldErrors, Throwable cause) {
    super(fieldErrors, cause);
  }
}
