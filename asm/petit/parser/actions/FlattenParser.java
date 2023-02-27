package petit.parser.actions;

import petit.parser.PetitParser;
import petit.parser.combinators.DelegateParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

/**
 * A parser that answers a flat copy of the range my delegate parses.
 */
public class FlattenParser extends DelegateParser {

  protected final String message;

  public FlattenParser(PetitParser delegate) {
    this(delegate, null);
  }

  public FlattenParser(PetitParser delegate, String message) {
    super(delegate);
    this.message = message;
  }

  @Override
  public Result parseOn(Context context) {
    if (message == null) {
      var result = delegate.parseOn(context);
      if (result.isSuccess()) {
        var flattened = context.getBuffer().substring(context.getPosition(), result.getPosition());
        return result.success(flattened);
      } else {
        return result;
      }
    } else {
      // If we have a message we can switch to fast mode.
      var position = delegate.fastParseOn(context.getBuffer(), context.getPosition());
      if (position < 0) {
        return context.failure(message);
      }
      var output = context.getBuffer().substring(context.getPosition(), position);
      return context.success(output, position);
    }
  }

  @Override
  public FlattenParser copy() {
    return new FlattenParser(delegate, message);
  }

}
