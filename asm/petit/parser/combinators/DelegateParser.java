package petit.parser.combinators;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A parser that delegates to another one.
 */
public class DelegateParser extends PetitParser {

  protected PetitParser delegate;

  public DelegateParser(PetitParser delegate) {
    this.delegate = Objects.requireNonNull(delegate, "Undefined delegate parser");
  }

  @Override
  public Result parseOn(Context context) {
    return delegate.parseOn(context);
  }

  @Override
  public void replace(PetitParser source, PetitParser target) {
    super.replace(source, target);
    if (delegate == source) {
      delegate = target;
    }
  }

  @Override
  public List<PetitParser> getChildren() {
    return Collections.singletonList(delegate);
  }

  @Override
  public DelegateParser copy() {
    return new DelegateParser(delegate);
  }

}
