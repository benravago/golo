package petit.parser.utils;

import petit.parser.PetitParser;
import petit.parser.combinators.DelegateParser;
import petit.parser.combinators.SettableParser;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Function;

/**
 * Tools to transform and optimize parser graphs.
 */
public class Optimizer {

  private final List<Function<PetitParser, PetitParser>> transformers = new ArrayList<>();

  /**
   * Adds a generic transformer.
   */
  public Optimizer add(Function<PetitParser, PetitParser> transformer) {
    transformers.add(transformer);
    return this;
  }

  /**
   * Adds a transformer that removes unnecessary delegates.
   */
  public Optimizer removeDelegates() {
    return add(parser -> {
      while (DelegateParser.class.equals(parser.getClass()) || SettableParser.class.equals(parser.getClass())) {
        parser = parser.getChildren().get(0);
      }
      return parser;
    });
  }

  /**
   * Adds a transformer that collapses unnecessary copies of parsers.
   */
  public Optimizer removeDuplicates() {
    var uniques = new HashSet<PetitParser>();
    return add(parser -> {
      var target = uniques.stream().filter(each -> parser != each && parser.isEqualTo(each)).findFirst();
      if (target.isPresent()) {
        return target.get();
      } else {
        uniques.add(parser);
        return parser;
      }
    });
  }

  /**
   * Transforms the provided parsers using the selected optimizations.
   */
  public PetitParser transform(PetitParser parser) {
    var transformer = transformers.stream().reduce(Function::andThen).orElse(Function.identity());
    return Mirror.of(parser).transform(transformer);
  }

}
