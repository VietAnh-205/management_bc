package ck4.nvb.rsmanagement.base.application.exception;

import java.io.Serial;

public class ObjectNotFoundException extends AppException {

  @Serial private static final long serialVersionUID = 1L;

  public ObjectNotFoundException() {
    super("No result found");
  }

  public ObjectNotFoundException(String message) {
    super(message);
  }

  public ObjectNotFoundException(String message, Object... arguments) {
    super(message, arguments);
  }

  public ObjectNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
