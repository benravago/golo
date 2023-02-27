package petit.parser.combinators;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A parser that parses a sequence of parsers.
 */
public class SequenceParser extends ListParser {

  public SequenceParser(PetitParser... parsers) {
    super(parsers);
  }

  @Override
  public Result parseOn(Context context) {
    var current = context;
    var elements = new ArrayList<Object>(parsers.length);
    for (var parser : parsers) {
      var result = parser.parseOn(current);
      if (result.isFailure()) {
        return result;
      }
      elements.add(result.get());
      current = result;
    }
    return current.success(elements);
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    for (var parser : parsers) {
      position = parser.fastParseOn(buffer, position);
      if (position < 0) {
        return position;
      }
    }
    return position;
  }

  @Override
  public SequenceParser seq(PetitParser... others) {
    var array = Arrays.copyOf(parsers, parsers.length + others.length);
    System.arraycopy(others, 0, array, parsers.length, others.length);
    return new SequenceParser(array);
  }

  @Override
  public SequenceParser copy() {
    return new SequenceParser(Arrays.copyOf(parsers, parsers.length));
  }

}
