package petit.parser.repeating;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.ArrayList;

/**
 * A greedy repeating parser, commonly seen in regular expression implementations.
 * It aggressively consumes as much input as possible and then backtracks to meet the 'limit' condition.
 */
public class GreedyRepeatingParser extends LimitedRepeatingParser {

  public GreedyRepeatingParser(PetitParser delegate, PetitParser limit, int min, int max) {
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
    var contexts = new ArrayList<Context>();
    contexts.add(current);
    while (max == UNBOUNDED || elements.size() < max) {
      var result = delegate.parseOn(current);
      if (result.isFailure()) {
        break;
      }
      elements.add(result.get());
      contexts.add(current = result);
    }
    for (;;) {
      var limiter = limit.parseOn(contexts.get(contexts.size() - 1));
      if (limiter.isSuccess()) {
        return contexts.get(contexts.size() - 1).success(elements);
      }
      if (elements.isEmpty()) {
        return limiter;
      }
      contexts.remove(contexts.size() - 1);
      elements.remove(elements.size() - 1);
      if (contexts.isEmpty()) {
        return limiter;
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
    var positions = new ArrayList<Integer>();
    positions.add(current);
    while (max == UNBOUNDED || count < max) {
      var result = delegate.fastParseOn(buffer, current);
      if (result < 0) {
        break;
      }
      positions.add(current = result);
      count++;
    }
    for (;;) {
      var limiter = limit.fastParseOn(buffer, positions.get(positions.size() - 1));
      if (limiter >= 0) {
        return positions.get(positions.size() - 1);
      }
      if (count == 0) {
        return -1;
      }
      positions.remove(positions.size() - 1);
      count--;
      if (positions.isEmpty()) {
        return -1;
      }
    }
  }

  @Override
  public GreedyRepeatingParser copy() {
    return new GreedyRepeatingParser(delegate, limit, min, max);
  }

}
