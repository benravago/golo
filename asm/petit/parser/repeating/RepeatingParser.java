package petit.parser.repeating;

import petit.parser.PetitParser;
import petit.parser.combinators.DelegateParser;

import java.util.Objects;

/**
 * An abstract parser that repeatedly parses between 'min' and 'max' instances of its delegate.
 */
public abstract class RepeatingParser extends DelegateParser {

  public static final int UNBOUNDED = -1;

  protected final int min;
  protected final int max;

  public RepeatingParser(PetitParser delegate, int min, int max) {
    super(delegate);
    this.min = min;
    this.max = max;
    if (min < 0) {
      throw new IllegalArgumentException("Invalid min repetitions: " + getRange());
    }
    if (max != UNBOUNDED && min > max) {
      throw new IllegalArgumentException("Invalid max repetitions: " + getRange());
    }
  }

  @Override
  public boolean hasEqualProperties(PetitParser other) {
    return super.hasEqualProperties(other)
        && Objects.equals(min, ((RepeatingParser) other).min)
        && Objects.equals(max, ((RepeatingParser) other).max);
  }

  @Override
  public String toString() {
    return super.toString() + "[" + getRange() + "]";
  }

  private String getRange() {
    return min + ".." + (max == UNBOUNDED ? "*" : max);
  }

}
