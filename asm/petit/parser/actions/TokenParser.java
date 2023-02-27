package petit.parser.actions;

import petit.parser.PetitParser;
import petit.parser.combinators.DelegateParser;
import petit.parser.context.Context;
import petit.parser.context.Result;
import petit.parser.context.Token;

/**
 * A parser that creates a token from the parsed input.
 */
public class TokenParser extends DelegateParser {

  public TokenParser(PetitParser delegate) {
    super(delegate);
  }

  @Override
  public Result parseOn(Context context) {
    var result = delegate.parseOn(context);
    if (result.isSuccess()) {
      var token = new Token(context.getBuffer(), context.getPosition(), result.getPosition(), result.get());
      return result.success(token);
    } else {
      return result;
    }
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    return delegate.fastParseOn(buffer, position);
  }

  @Override
  public TokenParser copy() {
    return new TokenParser(delegate);
  }

}
