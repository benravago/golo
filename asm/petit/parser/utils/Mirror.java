package petit.parser.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import petit.parser.PetitParser;

/**
 * A reflective parser mirror.
 */
public class Mirror implements Iterable<PetitParser> {

  /**
   * Constructs a mirror of the provided {@code parser}.
   */
  public static Mirror of(PetitParser parser) {
    return new Mirror(parser);
  }

  private final PetitParser parser;

  private Mirror(PetitParser parser) {
    this.parser = Objects.requireNonNull(parser, "Undefined parser");
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " of " + parser.toString();
  }

  /**
   * Returns an {@link Iterator} over {@code parser} and all its reachable descendants.
   */
  @Override
  public Iterator<PetitParser> iterator() {
    return new ParserIterator(parser);
  }

  private static class ParserIterator implements Iterator<PetitParser> {

    private final List<PetitParser> todo = new ArrayList<>();
    private final Set<PetitParser> seen = new HashSet<>();

    private ParserIterator(PetitParser root) {
      todo.add(root);
      seen.add(root);
    }

    @Override
    public boolean hasNext() {
      return !todo.isEmpty();
    }

    @Override
    public PetitParser next() {
      if (todo.isEmpty()) {
        throw new NoSuchElementException();
      }
      var current = todo.remove(todo.size() - 1);
      for (var parser : current.getChildren()) {
        if (!seen.contains(parser)) {
          todo.add(parser);
          seen.add(parser);
        }
      }
      return current;
    }
  }

  /**
   * Returns a {@link Stream} over {@code parser} and all its reachable descendants.
   */
  public Stream<PetitParser> stream() {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.DISTINCT | Spliterator.NONNULL), false);
  }

  /**
   * Returns a transformed copy of all parsers reachable from {@code parser}.
   */
  public PetitParser transform(Function<PetitParser, PetitParser> transformer) {
    var mapping = new HashMap<PetitParser, PetitParser>();
    for (var parser : this) {
      mapping.put(parser, transformer.apply(parser.copy()));
    }
    var seen = new HashSet<PetitParser>(mapping.values());
    var todo = new ArrayList<PetitParser>(mapping.values());
    while (!todo.isEmpty()) {
      var parent = todo.remove(todo.size() - 1);
      for (var child : parent.getChildren()) {
        if (mapping.containsKey(child)) {
          parent.replace(child, mapping.get(child));
        } else if (!seen.contains(child)) {
          seen.add(child);
          todo.add(child);
        }
      }
    }
    return mapping.get(parser);
  }

}
