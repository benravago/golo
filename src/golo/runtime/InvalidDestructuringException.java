package golo.runtime;

public class InvalidDestructuringException extends IllegalArgumentException {
  // TODO: localize the error message?

  public InvalidDestructuringException(String message) {
    super(message);
  }

  public InvalidDestructuringException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidDestructuringException(Throwable cause) {
    super(cause);
  }

  public static InvalidDestructuringException tooManyValues(int expected) {
    return new InvalidDestructuringException(String.format("too many values (expecting %d).", expected));
  }

  public static InvalidDestructuringException notEnoughValues(int expected, boolean sub) {
    return new InvalidDestructuringException(
        String.format("not enough values (expecting%s %d).", sub ? " at least" : "", sub ? expected - 1 : expected));
  }

  public static InvalidDestructuringException notEnoughValues(int expected, int available, boolean sub) {
    return new InvalidDestructuringException(String.format("not enough values (expecting%s %d and got %d).",
        sub ? " at least" : "", sub ? expected - 1 : expected, available));
  }

}
