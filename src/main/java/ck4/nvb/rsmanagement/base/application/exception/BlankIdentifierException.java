package ck4.nvb.rsmanagement.base.application.exception;

import java.io.Serial;
import java.text.MessageFormat;

public class BlankIdentifierException extends RuntimeException {

  @Serial private static final long serialVersionUID = -3404483383593320184L;

  /** Default empty constructor */
  public BlankIdentifierException() {}

  /**
   * General constructor to give the end user a helpful message that relates to why this error
   * occurred
   *
   * @param message helpful message string for the end user
   */
  public BlankIdentifierException(String message) {
    super(message);
  }

  /**
   * General constructor to give the end user a helpful message that relates to why this error
   * occurred.
   *
   * @param message helpful message string for the end user
   * @param arguments arguments of message string for the end user
   */
  public BlankIdentifierException(String message, Object... arguments) {
    super(MessageFormat.format(message, arguments));
  }

  /**
   * Convenience constructor used to simply wrap around a different error <code>cause</code>
   *
   * @param message helpful message string for the end user
   * @param cause parent exception cause that this exception is wrapping around
   */
  public BlankIdentifierException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor used to only wrap around a parent cause. Preference should be given to the {@link
   * #BlankIdentifierException(String, Throwable)} constructor before this one.
   *
   * @param cause the parent wrapping cause
   */
  public BlankIdentifierException(Throwable cause) {
    super(cause);
  }
}
