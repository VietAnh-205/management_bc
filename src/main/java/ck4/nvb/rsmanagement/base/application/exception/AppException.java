package ck4.nvb.rsmanagement.base.application.exception;

import ck4.nvb.rsmanagement.base.web.error.FieldError;
import java.io.Serial;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException {
  @Serial private static final long serialVersionUID = 12121212L;

  private List<FieldError> fieldErrors;

  /**
   * Default empty constructor. If at all possible, don't use this one, but use the {@link
   * #AppException(String)} constructor to specify a helpful message to the end user
   */
  public AppException() {}

  public AppException(List<FieldError> fieldErrors) {
    this.fieldErrors = fieldErrors;
  }

  /**
   * General constructor to give the end user a helpful message that relates to why this error
   * occurred.
   *
   * @param message helpful message string for the end user
   */
  public AppException(String message) {
    super(message);
  }

  public AppException(List<FieldError> fieldErrors, String message) {
    this(message);
    this.fieldErrors = fieldErrors;
  }

  /**
   * General constructor to give the end user a helpful message that relates to why this error
   * occurred.
   *
   * @param message helpful message string for the end user
   * @param arguments arguments of message string for the end user
   */
  public AppException(String message, Object... arguments) {
    super(MessageFormat.format(message, arguments));
  }

  public AppException(List<FieldError> fieldErrors, String message, Object... arguments) {
    this(message, arguments);
    this.fieldErrors = fieldErrors;
  }

  /**
   * General constructor to give the end user a helpful message and to also propagate the parent
   * error exception message.
   *
   * @param message helpful message string for the end user
   * @param cause the parent exception cause that this AppException is wrapping around
   */
  public AppException(String message, Throwable cause) {
    super(message, cause);
  }

  public AppException(List<FieldError> fieldErrors, String message, Throwable cause) {
    this(message, cause);
    this.fieldErrors = fieldErrors;
  }

  /**
   * Constructor used to simply chain a parent exception cause to an AppException. Preference should
   * be given to the {@link #AppException(String, Throwable)} constructor if at all possible instead
   * of this one.
   *
   * @param cause the parent exception cause that this AppException is wrapping around
   */
  public AppException(Throwable cause) {
    super(cause);
  }

  public AppException(List<FieldError> fieldErrors, Throwable cause) {
    this(cause);
    this.fieldErrors = fieldErrors;
  }
}
