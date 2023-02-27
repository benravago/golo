package petit.parser.combinators;

import petit.parser.PetitParser;
import petit.parser.primitive.FailureParser;

/**
 * A parser that can be set to behave like another parser.
 */
public class SettableParser extends DelegateParser {

  /**
   * Constructs a {@link SettableParser} that currently refers to an {@link FailureParser}.
   */
  public static SettableParser undefined() {
    return undefined("Undefined parser");
  }

  /**
   * Constructs a {@link SettableParser} that currently refers to an {@link FailureParser} with the provided {@code message}.
   */
  public static SettableParser undefined(String message) {
    return with(FailureParser.withMessage(message));
  }

  /**
   * Constructs a {@link SettableParser} referring to the supplied {@code parser}.
   */
  public static SettableParser with(PetitParser parser) {
    return new SettableParser(parser);
  }

  public SettableParser(PetitParser delegate) {
    super(delegate);
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    return delegate.fastParseOn(buffer, position);
  }

  /**
   * Return the current referred parser.
   */
  public PetitParser get() {
    return delegate;
  }

  /**
   * Replace the current referred parser with a new {@code delegate}.
   */
  public void set(PetitParser delegate) {
    this.delegate = delegate;
  }

  @Override
  public SettableParser copy() {
    return new SettableParser(delegate);
  }

}
