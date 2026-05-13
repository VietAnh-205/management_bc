package ck4.nvb.rsmanagement.base.application.exception;

import java.io.Serial;

public class AppPersistenceException extends AppException {

  @Serial private static final long serialVersionUID = 1L;

  public AppPersistenceException() {}

  public AppPersistenceException(String message) {
    super(message);
  }

  public AppPersistenceException(String message, Object... arguments) {
    super(message, arguments);
  }

  public AppPersistenceException(String message, Throwable cause) {
    super(message, cause);
  }

  public AppPersistenceException(Throwable cause) {
    super(cause);
  }
}
