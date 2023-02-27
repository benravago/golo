package petit.parser.primitive;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.Objects;

/**
 * A parser that consumes nothing and always fails.
 */
public class FailureParser extends PetitParser {

  /**
   * Construct a {@link FailureParser} that fails with the supplied {@code message}.
   */
  public static PetitParser withMessage(String message) {
    return new FailureParser(message);
  }

  private final String message;

  private FailureParser(String message) {
    this.message = Objects.requireNonNull(message, "Undefined message");
  }

  @Override
  public Result parseOn(Context context) {
    return context.failure(message);
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    return -1;
  }

  @Override
  protected boolean hasEqualProperties(PetitParser other) {
    return super.hasEqualProperties(other) && Objects.equals(message, ((FailureParser) other).message);
  }

  @Override
  public FailureParser copy() {
    return new FailureParser(message);
  }

  @Override
  public String toString() {
    return super.toString() + "[" + message + "]";
  }

}
