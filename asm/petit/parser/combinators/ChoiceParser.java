package petit.parser.combinators;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Failure;
import petit.parser.context.Result;
import petit.parser.utils.FailureJoiner;

import java.util.Arrays;

/**
 * A parser that uses the first parser that succeeds.
 */
public class ChoiceParser extends ListParser {

  protected final FailureJoiner failureJoiner;

  public ChoiceParser(PetitParser... parsers) {
    this(new FailureJoiner.SelectLast(), parsers);
  }

  public ChoiceParser(FailureJoiner failureJoiner, PetitParser... parsers) {
    super(parsers);
    this.failureJoiner = failureJoiner;
    if (parsers.length == 0) {
      throw new IllegalArgumentException("Choice parser cannot be empty.");
    }
  }

  @Override
  public Result parseOn(Context context) {
    Failure failure = null;
    for (var parser : parsers) {
      var result = parser.parseOn(context);
      if (result.isFailure()) {
        failure = failure == null ? (Failure) result : failureJoiner.apply(failure, (Failure) result);
      } else {
        return result;
      }
    }
    return failure;
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    var result = -1;
    for (var parser : parsers) {
      result = parser.fastParseOn(buffer, position);
      if (result >= 0) {
        return result;
      }
    }
    return result;
  }

  @Override
  public ChoiceParser or(FailureJoiner failureJoiner, PetitParser... others) {
    var array = Arrays.copyOf(parsers, parsers.length + others.length);
    System.arraycopy(others, 0, array, parsers.length, others.length);
    return new ChoiceParser(failureJoiner, array);
  }

  @Override
  public ChoiceParser copy() {
    return new ChoiceParser(failureJoiner, Arrays.copyOf(parsers, parsers.length));
  }

}
