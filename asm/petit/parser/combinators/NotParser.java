package petit.parser.combinators;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.Objects;

/**
 * The not-predicate, a parser that succeeds whenever its delegate does not, but consumes no input [Parr 1994, 1995].
 */
public class NotParser extends DelegateParser {

  protected final String message;

  public NotParser(PetitParser delegate, String message) {
    super(delegate);
    this.message = Objects.requireNonNull(message, "Undefined message");
  }

  @Override
  public Result parseOn(Context context) {
    var result = delegate.parseOn(context);
    return result.isFailure() ? context.success(null) : context.failure(message);
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    var result = delegate.fastParseOn(buffer, position);
    return result < 0 ? position : -1;
  }

  @Override
  protected boolean hasEqualProperties(PetitParser other) {
    return super.hasEqualProperties(other) && Objects.equals(message, ((NotParser) other).message);
  }

  @Override
  public NotParser copy() {
    return new NotParser(delegate, message);
  }

  @Override
  public String toString() {
    return super.toString() + "[" + message + "]";
  }

}
