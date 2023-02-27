package petit.parser.primitive;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

/**
 * A parser that consumes nothing and always succeeds.
 */
public class EpsilonParser extends PetitParser {

  @Override
  public Result parseOn(Context context) {
    return context.success(null);
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    return position;
  }

  @Override
  public EpsilonParser copy() {
    return new EpsilonParser();
  }

}
