package petit.parser.combinators;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import petit.parser.PetitParser;

/**
 * Abstract parser that parses a list of things in some way (to be specified by the subclasses).
 */
public abstract class ListParser extends PetitParser {

  protected final PetitParser[] parsers;

  public ListParser(PetitParser... parsers) {
    this.parsers = Objects.requireNonNull(parsers, "Undefined parser list");
  }

  @Override
  public void replace(PetitParser source, PetitParser target) {
    super.replace(source, target);
    for (var i = 0; i < parsers.length; i++) {
      if (parsers[i] == source) {
        parsers[i] = target;
      }
    }
  }

  @Override
  public List<PetitParser> getChildren() {
    return Arrays.asList(parsers);
  }

}
