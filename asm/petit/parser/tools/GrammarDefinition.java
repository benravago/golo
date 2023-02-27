package petit.parser.tools;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helper to conveniently define and build complex, recursive grammars using plain Java code.
 * <p>
 * To create a new grammar definition subclass {@link GrammarDefinition}.
 * For every production call {@link GrammarDefinition#def(String, PetitParser)} giving the parsers a name.
 * The start production should be named 'start'.
 * <p>
 * To refer to other productions use {@link GrammarDefinition#ref(String)}.
 * To redefine or attach actions to productions use {@link GrammarDefinition#redef(String, Function)}, {@link GrammarDefinition#redef(String, petit.parser.PetitParser)} and {@link GrammarDefinition#action(String, Function)}.
 * <p>
 * To build the resulting grammar call {@link GrammarDefinition#build()}, or wrap it in the class {@link GrammarParser}.
 */
public class GrammarDefinition {

  private final Map<String, PetitParser> parsers = new HashMap<>();

  /**
   * Returns a reference to the production with the given {@code name}.
   */
  protected final PetitParser ref(String name) {
    return new Reference(name);
  }

  /**
   * Defines a production with a {@code name} and a {@code parser}.
   */
  protected final void def(String name, PetitParser parser) {
    if (parsers.containsKey(name)) {
      throw new IllegalStateException("Duplicate production: " + name);
    }
    parsers.put(Objects.requireNonNull(name), Objects.requireNonNull(parser));
  }

  /**
   * Redefines an existing production with a {@code name} and a new {@code parser}.
   */
  protected final void redef(String name, PetitParser parser) {
    if (!parsers.containsKey(name)) {
      throw new IllegalStateException("Undefined production: " + name);
    }
    parsers.put(Objects.requireNonNull(name), Objects.requireNonNull(parser));
  }

  /**
   * Redefines an existing production with a {@code name} and a {@code function} producing a new parser.
   * Only call this method during initialization.
   */
  protected final void redef(String name, Function<PetitParser, PetitParser> function) {
    if (!parsers.containsKey(name)) {
      throw new IllegalStateException("Undefined production: " + name);
    }
    redef(name, function.apply(parsers.get(name)));
  }

  /**
   * Attaches an action {@code function} to an existing production {@code name}.
   * Only call this method during initialization.
   */
  protected final <S, T> void action(String name, Function<S, T> function) {
    redef(name, parser -> parser.map(function));
  }

  /**
   * Builds a parser starting from the production {@code "start"}.
   */
  public PetitParser build() {
    return build("start");
  }

  /**
   * Builds a parser starting from the provided production {@code name}.
   */
  public PetitParser build(String name) {
    return resolve(new Reference(name));
  }

  private PetitParser resolve(Reference reference) {
    var mapping = new HashMap<Reference, PetitParser>();
    var todo = new ArrayList<PetitParser>();
    todo.add(dereference(mapping, reference));
    var seen = new HashSet<PetitParser>(todo);
    while (!todo.isEmpty()) {
      var parent = todo.remove(todo.size() - 1);
      for (var child : parent.getChildren()) {
        if (child instanceof Reference childReference) {
          var referenced = dereference(mapping, childReference);
          parent.replace(child, referenced);
          child = referenced;
        }
        if (!seen.contains(child)) {
          seen.add(child);
          todo.add(child);
        }
      }
    }
    return mapping.get(reference);
  }

  private PetitParser dereference(Map<Reference, PetitParser> mapping, Reference reference) {
    var parser = mapping.get(reference);
    if (parser == null) {
      var references = new ArrayList<Reference>();
      references.add(reference);
      parser = reference.resolve();
      while (parser instanceof Reference otherReference) {
        if (references.contains(otherReference)) {
          throw new IllegalStateException("Recursive references detected: " + String.join(", ", references.stream().map(ref -> ref.name).collect(Collectors.joining(", "))));
        }
        references.add(otherReference);
        parser = otherReference.resolve();
      }
      for (var otherReference : references) {
        mapping.put(otherReference, parser);
      }
    }
    return parser;
  }

  private class Reference extends PetitParser {

    private final String name;

    private Reference(String name) {
      this.name = Objects.requireNonNull(name);
    }

    private PetitParser resolve() {
      if (!parsers.containsKey(name)) {
        throw new IllegalStateException("Unknown parser reference: " + name);
      }
      return parsers.get(name);
    }

    @Override
    public Result parseOn(Context context) {
      throw new UnsupportedOperationException("References cannot be parsed.");
    }

    @Override
    public PetitParser copy() {
      throw new UnsupportedOperationException("References cannot be copied.");
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (other == null || getClass() != other.getClass()) {
        return false;
      }
      Reference reference = (Reference) other;
      return Objects.equals(name, reference.name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }

}