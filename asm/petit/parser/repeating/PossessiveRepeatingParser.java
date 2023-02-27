package petit.parser.repeating;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.ArrayList;

/**
 * A greedy parser that repeatedly parses between 'min' and 'max' instances of its delegate.
 */
public class PossessiveRepeatingParser extends RepeatingParser {

  public PossessiveRepeatingParser(PetitParser delegate, int min, int max) {
    super(delegate, min, max);
  }

  @Override
  public Result parseOn(Context context) {
    var current = context;
    var elements = new ArrayList<Object>();
    while (elements.size() < min) {
      var result = delegate.parseOn(current);
      if (result.isFailure()) {
        return result;
      }
      elements.add(result.get());
      current = result;
    }
    while (max == UNBOUNDED || elements.size() < max) {
      var result = delegate.parseOn(current);
      if (result.isFailure()) {
        return current.success(elements);
      }
      elements.add(result.get());
      current = result;
    }
    return current.success(elements);
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    var count = 0;
    var current = position;
    while (count < min) {
      var result = delegate.fastParseOn(buffer, current);
      if (result < 0) {
        return result;
      }
      current = result;
      count++;
    }
    while (max == UNBOUNDED || count < max) {
      var result = delegate.fastParseOn(buffer, current);
      if (result < 0) {
        return current;
      }
      current = result;
      count++;
    }
    return current;
  }

  @Override
  public PossessiveRepeatingParser copy() {
    return new PossessiveRepeatingParser(delegate, min, max);
  }

}
