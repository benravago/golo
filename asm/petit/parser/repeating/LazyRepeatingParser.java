package petit.parser.repeating;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.ArrayList;

/**
 * A lazy repeating parser, commonly seen in regular expression implementations.
 * It limits its consumption to meet the 'limit' condition as early as possible.
 */
public class LazyRepeatingParser extends LimitedRepeatingParser {

  public LazyRepeatingParser(PetitParser delegate, PetitParser limit, int min, int max) {
    super(delegate, limit, min, max);
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
    for (;;) {
      var limiter = limit.parseOn(current);
      if (limiter.isSuccess()) {
        return current.success(elements);
      } else {
        if (max != UNBOUNDED && elements.size() >= max) {
          return limiter;
        }
        var result = delegate.parseOn(current);
        if (result.isFailure()) {
          return limiter;
        }
        elements.add(result.get());
        current = result;
      }
    }
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    var count = 0;
    var current = position;
    while (count < min) {
      var result = delegate.fastParseOn(buffer, current);
      if (result < 0) {
        return -1;
      }
      current = result;
      count++;
    }
    for (;;) {
      var limiter = limit.fastParseOn(buffer, current);
      if (limiter >= 0) {
        return current;
      } else {
        if (max != UNBOUNDED && count >= max) {
          return -1;
        }
        var result = delegate.fastParseOn(buffer, current);
        if (result < 0) {
          return -1;
        }
        current = result;
        count++;
      }
    }
  }

  @Override
  public LazyRepeatingParser copy() {
    return new LazyRepeatingParser(delegate, limit, min, max);
  }

}
