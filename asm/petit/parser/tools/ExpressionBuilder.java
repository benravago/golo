package petit.parser.tools;

import petit.parser.PetitParser;
import petit.parser.combinators.ChoiceParser;
import petit.parser.combinators.SequenceParser;
import petit.parser.combinators.SettableParser;
import petit.parser.primitive.FailureParser;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

/**
 * A builder that allows the simple definition of expression grammars with prefix, postfix, and left- and right-associative infix operators.
 */
public class ExpressionBuilder {

  private final SettableParser loopback = SettableParser.undefined();
  private final List<ExpressionGroup> groups = new ArrayList<>();

  /**
   * Creates a new group of operators that share the same priority.
   */
  public ExpressionGroup group() {
    ExpressionGroup group = new ExpressionGroup();
    groups.add(group);
    return group;
  }

  /**
   * Builds the expression parser.
   */
  public PetitParser build() {
    var parser = FailureParser.withMessage("Highest priority group should define a primitive parser.");
    for (var group : groups) {
      parser = group.build(parser);
    }
    loopback.set(parser);
    return parser;
  }

  /**
   * Models a group of operators of the same precedence.
   */
  public class ExpressionGroup {

    private final List<PetitParser> primitives = new ArrayList<>();
    private final List<PetitParser> wrappers = new ArrayList<>();
    private final List<PetitParser> prefix = new ArrayList<>();
    private final List<PetitParser> postfix = new ArrayList<>();
    private final List<PetitParser> right = new ArrayList<>();
    private final List<PetitParser> left = new ArrayList<>();

    /**
     * Defines a new primitive or literal {@code parser}.
     */
    public ExpressionGroup primitive(PetitParser parser) {
      return primitive(parser, null);
    }

    /**
     * Defines a new primitive or literal {@code parser}.
     * Evaluates the optional {@code action} with the parsed {@code value}.
     */
    public <T, R> ExpressionGroup primitive(PetitParser parser, /*@Nullable*/ Function<T, R> action) {
      primitives.add(action == null ? parser : parser.map(action));
      return this;
    }

    private PetitParser buildPrimitive(PetitParser inner) {
      return buildChoice(primitives, inner);
    }

    /**
     * Defines a new wrapper using {@code left} and {@code right} parsers.
     */
    public ExpressionGroup wrapper(PetitParser left, PetitParser right) {
      return wrapper(left, right, null);
    }

    /**
     * Defines a new wrapper using {@code left} and {@code right} parsers.
     * Evaluates the optional {@code action} with the parsed {@code left}, {@code value} and {@code right}.
     */
    public <T, R> ExpressionGroup wrapper(PetitParser left, PetitParser right, /*@Nullable*/ Function<T, R> action) {
      var parser = new SequenceParser(left, loopback, right);
      wrappers.add(action == null ? parser : parser.map(action));
      return this;
    }

    private PetitParser buildWrapper(PetitParser inner) {
      var choices = new ArrayList<PetitParser>(wrappers);
      choices.add(inner);
      return buildChoice(choices, inner);
    }

    /**
     * Adds a prefix operator {@code parser}.
     */
    public ExpressionGroup prefix(PetitParser parser) {
      return prefix(parser, null);
    }

    /**
     * Adds a prefix operator {@code parser}.
     * Evaluates the optional {@code action} with the parsed {@code operator} and {@code value}.
     */
    public <T, R> ExpressionGroup prefix(PetitParser parser, /*@Nullable*/ Function<T, R> action) {
      addTo(prefix, parser, action);
      return this;
    }

    private PetitParser buildPrefix(PetitParser inner) {
      if (prefix.isEmpty()) {
        return inner;
      } else {
        var sequence = new SequenceParser(buildChoice(prefix).star(), inner);
        return sequence.map((List<List<ExpressionResult>> tuple) -> {
          var value = (Object)tuple.get(1);
          var tuples = tuple.get(0);
          Collections.reverse(tuples);
          for (var result : tuples) {
            value = result.action.apply(Arrays.asList(result.operator, value));
          }
          return value;
        });
      }
    }

    /**
     * Adds a postfix operator {@code parser}.
     */
    public ExpressionGroup postfix(PetitParser parser) {
      return postfix(parser, null);
    }

    /**
     * Adds a postfix operator {@code parser}. Evaluates the optional {@code action} with the parsed {@code value} and {@code operator}.
     */
    public <T, R> ExpressionGroup postfix(PetitParser parser, /*@Nullable*/ Function<T, R> action) {
      addTo(postfix, parser, action);
      return this;
    }

    private PetitParser buildPostfix(PetitParser inner) {
      if (postfix.isEmpty()) {
        return inner;
      } else {
        var sequence = new SequenceParser(inner, buildChoice(postfix).star());
        return sequence.map((List<List<ExpressionResult>> tuple) -> {
          var value = (Object)tuple.get(0);
          for (var result : tuple.get(1)) {
            value = result.action.apply(Arrays.asList(value, result.operator));
          }
          return value;
        });
      }
    }

    /**
     * Adds a right-associative operator {@code parser}.
     */
    public ExpressionGroup right(PetitParser parser) {
      return right(parser, null);
    }

    /**
     * Adds a right-associative operator {@code parser}. Evaluates the optional
     * {@code action} with the parsed {@code left} term, {@code operator}, and
     * {@code right} term.
     */
    public <T, R> ExpressionGroup right(PetitParser parser, /*@Nullable*/ Function<T, R> action) {
      addTo(right, parser, action);
      return this;
    }

    private PetitParser buildRight(PetitParser inner) {
      if (right.isEmpty()) {
        return inner;
      } else {
        var sequence = inner.separatedBy(buildChoice(right));
        return sequence.map((List<Object> innerSequence) -> {
          var result = innerSequence.get(innerSequence.size() - 1);
          for (var i = innerSequence.size() - 2; i > 0; i -= 2) {
            var expressionResult = (ExpressionResult) innerSequence.get(i);
            result = expressionResult.action.apply(Arrays.asList(innerSequence.get(i - 1), expressionResult.operator, result));
          }
          return result;
        });
      }
    }

    /**
     * Adds a left-associative operator {@code parser}.
     */
    public ExpressionGroup left(PetitParser parser) {
      return left(parser, null);
    }

    /**
     * Adds a left-associative operator {@code parser}. Evaluates the optional
     * {@code action} with the parsed {@code left} term, {@code operator}, and
     * {@code right} term.
     */
    public <T, R> ExpressionGroup left(PetitParser parser, /*@Nullable*/ Function<T, R> action) {
      addTo(left, parser, action);
      return this;
    }

    private PetitParser buildLeft(PetitParser inner) {
      if (left.isEmpty()) {
        return inner;
      } else {
        var sequence = inner.separatedBy(buildChoice(left));
        return sequence.map((List<Object> innerSequence) -> {
          var result = innerSequence.get(0);
          for (var i = 1; i < innerSequence.size(); i += 2) {
            var expressionResult = (ExpressionResult) innerSequence.get(i);
            result = expressionResult.action.apply(Arrays.asList(result, expressionResult.operator, innerSequence.get(i + 1)));
          }
          return result;
        });
      }
    }

    // helper to connect operator parser and action, and add to list
    @SuppressWarnings("unchecked")
    private <T, R> void addTo(List<PetitParser> list, PetitParser parser, /*@Nullable*/ Function<T, R> action) {
      var mapper = action == null ? Function.identity() : (Function<Object, Object>) action;
      list.add(parser.map(operator -> new ExpressionResult(operator, mapper)));
    }

    // helper to build an optimal choice parser
    private PetitParser buildChoice(List<PetitParser> parsers) {
      return buildChoice(parsers, null);
    }

    private PetitParser buildChoice(List<PetitParser> parsers, PetitParser otherwise) {
      if (parsers.isEmpty()) {
        return otherwise;
      } else if (parsers.size() == 1) {
        return parsers.get(0);
      } else {
        return new ChoiceParser(parsers.toArray(new PetitParser[parsers.size()]));
      }
    }

    // helper to build the group of parsers
    private PetitParser build(PetitParser inner) {
      return buildLeft(buildRight(buildPostfix(buildPrefix(buildWrapper(buildPrimitive(inner))))));
    }
  }

  // helper class to associate operators and actions
  private static class ExpressionResult {
    final Object operator;
    final Function<Object, Object> action;

    private ExpressionResult(Object operator, Function<Object, Object> action) {
      this.operator = operator;
      this.action = action;
    }
  }

}
