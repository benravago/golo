package petit.parser.actions;

import petit.parser.PetitParser;
import petit.parser.combinators.DelegateParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A parser that silently consumes a before and after the delegate parser.
 */
public class TrimmingParser extends DelegateParser {

  private PetitParser left;
  private PetitParser right;

  public TrimmingParser(PetitParser delegate, PetitParser left, PetitParser right) {
    super(delegate);
    this.left = Objects.requireNonNull(left, "Undefined left trimming parser");
    this.right = Objects.requireNonNull(right, "Undefined right trimming parser");
  }

  @Override
  public Result parseOn(Context context) {
    var buffer = context.getBuffer();

    // Trim the left part:
    var before = consume(left, buffer, context.getPosition());
    if (before != context.getPosition()) {
      context = new Context(buffer, before);
    }

    // Consume the delegate:
    var result = delegate.parseOn(context);
    if (result.isFailure()) {
      return result;
    }

    // Trim the right part:
    var after = consume(right, buffer, result.getPosition());
    return after == result.getPosition() ? result : result.success(result.get(), after);
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    var result = delegate.fastParseOn(buffer, consume(left, buffer, position));
    return result < 0 ? result : consume(right, buffer, result);
  }

  private int consume(PetitParser parser, String buffer, int position) {
    for (;;) {
      var result = parser.fastParseOn(buffer, position);
      if (result < 0) {
        return position;
      }
      position = result;
    }
  }

  @Override
  public void replace(PetitParser source, PetitParser target) {
    super.replace(source, target);
    if (left == source) {
      left = target;
    }
    if (right == source) {
      right = target;
    }
  }

  @Override
  public List<PetitParser> getChildren() {
    return Arrays.asList(delegate, left, right);
  }

  @Override
  public TrimmingParser copy() {
    return new TrimmingParser(delegate, left, right);
  }

}
