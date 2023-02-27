package petit.parser.combinators;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.Objects;

/**
 * A parser that succeeds only at the end of the input stream.
 */
public class EndOfInputParser extends PetitParser {

  protected final String message;

  public EndOfInputParser(String message) {
    this.message = Objects.requireNonNull(message, "Undefined message");
  }

  @Override
  public Result parseOn(Context context) {
    return context.getPosition() < context.getBuffer().length() ? context.failure(message) : context.success(null);
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    return position < buffer.length() ? -1 : position;
  }

  @Override
  protected boolean hasEqualProperties(PetitParser other) {
    return super.hasEqualProperties(other) && Objects.equals(message, ((EndOfInputParser) other).message);
  }

  @Override
  public EndOfInputParser copy() {
    return new EndOfInputParser(message);
  }

  @Override
  public String toString() {
    return super.toString() + "[" + message + "]";
  }

}
