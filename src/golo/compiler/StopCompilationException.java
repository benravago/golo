package golo.compiler;

/**
 * Exception to stop the compilation process.
 *
 * <p>No stacktrace is available to make it lightweight.
 * Usefull in macros to stop the compilation without all the usual error stuffs.
 */
public class StopCompilationException extends RuntimeException {
  public StopCompilationException() {
    this(null, null);
  }

  public StopCompilationException(String message) {
    this(message, null);
  }

  public StopCompilationException(Throwable cause) {
    this(null, cause);
  }

  public StopCompilationException(String message, Throwable cause) {
    super(message, cause, true, false);
  }

  @Override
  public Throwable fillInStackTrace() {
    return this;
  }
}
