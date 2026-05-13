package ck4.nvb.rsmanagement.base.application.exception;

import java.io.Serial;

public class InvalidFormatException extends AppException {

  @Serial private static final long serialVersionUID = 1L;

  public InvalidFormatException() {}

  public InvalidFormatException(String message) {
    super(message);
  }

  public InvalidFormatException(String message, Object... arguments) {
    super(message, arguments);
  }

  public InvalidFormatException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidFormatException(Throwable cause) {
    super(cause);
  }
}
