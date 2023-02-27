package petit.parser.repeating;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import petit.parser.PetitParser;

/**
 * An abstract parser that repeatedly parses between 'min' and 'max' instances of its delegate and that requires the input to be completed with a specified parser 'limit'.
 * Subclasses provide repeating behavior as typically seen in regular expression implementations (non-blind).
 */
public abstract class LimitedRepeatingParser extends RepeatingParser {

  protected PetitParser limit;

  public LimitedRepeatingParser(PetitParser delegate, PetitParser limit, int min, int max) {
    super(delegate, min, max);
    this.limit = Objects.requireNonNull(limit, "Undefined limit parser");
  }

  @Override
  public List<PetitParser> getChildren() {
    return Arrays.asList(delegate, limit);
  }

  @Override
  public void replace(PetitParser source, PetitParser target) {
    super.replace(source, target);
    if (limit == source) {
      limit = target;
    }
  }

}
