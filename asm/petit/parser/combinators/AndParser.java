package petit.parser.combinators;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

/**
 * The and-predicate, a parser that succeeds whenever its delegate does, but does not consume the input stream [Parr 1994, 1995].
 */
public class AndParser extends DelegateParser {

  public AndParser(PetitParser delegate) {
    super(delegate);
  }

  @Override
  public Result parseOn(Context context) {
    var result = delegate.parseOn(context);
    return result.isSuccess() ? context.success(result.get()) : result;
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    var result = delegate.fastParseOn(buffer, position);
    return result < 0 ? -1 : position;
  }

  @Override
  public AndParser copy() {
    return new AndParser(delegate);
  }

}
