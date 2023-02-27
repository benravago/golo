package petit.parser.actions;

import petit.parser.PetitParser;
import petit.parser.combinators.DelegateParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.Objects;
import java.util.function.Function;

/**
 * A parser that performs a transformation with a given function on the successful parse result of the delegate.
 *
 * @param <T> The type of the function argument.
 * @param <R> The type of the function result.
 */
public class ActionParser<T, R> extends DelegateParser {

  protected final Function<T, R> function;
  protected final boolean hasSideEffects;

  public ActionParser(PetitParser delegate, Function<T, R> function) {
    this(delegate, function, false);
  }

  public ActionParser(PetitParser delegate, Function<T, R> function, boolean hasSideEffects) {
    super(delegate);
    this.function = Objects.requireNonNull(function, "Undefined function");
    this.hasSideEffects = hasSideEffects;
  }

  @Override
  public Result parseOn(Context context) {
    var result = delegate.parseOn(context);
    return result.isSuccess() ? result.success(function.apply(result.get())) : result;
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    // If we know to have side-effects, we have to fall back to the slow mode.
    return hasSideEffects ? super.fastParseOn(buffer, position) : delegate.fastParseOn(buffer, position);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected boolean hasEqualProperties(PetitParser other) {
    return super.hasEqualProperties(other)
        && Objects.equals(function, ((ActionParser<T, R>) other).function)
        && hasSideEffects == ((ActionParser<T, R>) other).hasSideEffects;
  }

  @Override
  public ActionParser<T, R> copy() {
    return new ActionParser<>(delegate, function, hasSideEffects);
  }

}
