package ck4.nvb.rsmanagement.base.application.exception;

import ck4.nvb.rsmanagement.base.web.error.FieldError;
import java.io.Serial;
import java.util.List;

public class AppAuthenticationException extends AppException {

  @Serial private static final long serialVersionUID = 1L;

  public AppAuthenticationException() {}

  public AppAuthenticationException(List<FieldError> fieldErrors) {
    super(fieldErrors);
  }

  public AppAuthenticationException(String message) {
    super(message);
  }

  public AppAuthenticationException(List<FieldError> fieldErrors, String message) {
    super(fieldErrors, message);
  }

  public AppAuthenticationException(String message, Object... arguments) {
    super(message, arguments);
  }

  public AppAuthenticationException(
      List<FieldError> fieldErrors, String message, Object... arguments) {
    super(fieldErrors, message, arguments);
  }

  public AppAuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }

  public AppAuthenticationException(List<FieldError> fieldErrors, String message, Throwable cause) {
    super(fieldErrors, message, cause);
  }

  public AppAuthenticationException(Throwable cause) {
    super(cause);
  }

  public AppAuthenticationException(List<FieldError> fieldErrors, Throwable cause) {
    super(fieldErrors, cause);
  }
}
