package golo.runtime;

public class AmbiguousFunctionReferenceException extends ReflectiveOperationException {

  public AmbiguousFunctionReferenceException(String message) {
    super(message);
  }

  public AmbiguousFunctionReferenceException(String message, Throwable cause) {
    super(message, cause);
  }

  public AmbiguousFunctionReferenceException(Throwable cause) {
    super(cause);
  }
}
