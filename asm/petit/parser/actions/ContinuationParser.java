package petit.parser.actions;

import petit.parser.PetitParser;
import petit.parser.combinators.DelegateParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.Objects;
import java.util.function.Function;

/**
 * Continuation parser that when activated captures a continuation function and passes it together with the current context into the handler.
 */
public class ContinuationParser extends DelegateParser {

  @FunctionalInterface
  public interface ContinuationHandler {
    Result apply(Function<Context, Result> continuation, Context context);
  }

  private final ContinuationHandler handler;

  public ContinuationParser(PetitParser delegate, ContinuationHandler handler) {
    super(delegate);
    this.handler = Objects.requireNonNull(handler, "Undefined handler");
  }

  @Override
  public Result parseOn(Context context) {
    return handler.apply(super::parseOn, context);
  }

  @Override
  protected boolean hasEqualProperties(PetitParser other) {
    return super.hasEqualProperties(other) && Objects.equals(handler, ((ContinuationParser) other).handler);
  }

  @Override
  public ContinuationParser copy() {
    return new ContinuationParser(delegate, handler);
  }

}
