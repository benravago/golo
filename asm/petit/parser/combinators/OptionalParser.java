package petit.parser.combinators;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.Objects;

/**
 * A parser that optionally parsers its delegate, or answers nil.
 */
public class OptionalParser extends DelegateParser {

  protected final Object otherwise;

  public OptionalParser(PetitParser delegate, /*@Nullable*/ Object otherwise) {
    super(delegate);
    this.otherwise = otherwise;
  }

  @Override
  public Result parseOn(Context context) {
    var result = delegate.parseOn(context);
    return result.isSuccess() ? result : context.success(otherwise);
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    var result = delegate.fastParseOn(buffer, position);
    return result < 0 ? position : result;
  }

  @Override
  protected boolean hasEqualProperties(PetitParser other) {
    return super.hasEqualProperties(other) && Objects.equals(otherwise, ((OptionalParser) other).otherwise);
  }

  @Override
  public OptionalParser copy() {
    return new OptionalParser(delegate, otherwise);
  }

}
