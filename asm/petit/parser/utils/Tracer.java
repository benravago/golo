package petit.parser.utils;

import petit.parser.PetitParser;
import petit.parser.context.Context;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Traces the activation and return of parsers.
 */
public class Tracer {

  /**
   * Returns a parser that calls the provided consumer with a {@link TraceEvent} whenever a parser is activated or returning.
   */
  public static PetitParser on(PetitParser source, Consumer<TraceEvent> consumer) {
    var parentClosure = new TraceEvent[1];
    return Mirror.of(source).transform(parser -> parser.callCC((continuation, context) -> {
      var parent = parentClosure[0];
      var enter = new TraceEvent(TraceEventType.ENTER, parent, parser, context);
      consumer.accept(enter);
      parentClosure[0] = enter;
      var result = continuation.apply(context);
      parentClosure[0] = parent;
      var exit = new TraceEvent(TraceEventType.EXIT, parent, parser, result);
      consumer.accept(exit);
      return result;
    }));
  }

  /**
   * The trace event type differentiating between activation and return.
   */
  public enum TraceEventType {
    ENTER, EXIT
  }

  /**
   * The trace event holding all relevant data.
   */
  public static class TraceEvent {

    /**
     * The type of this event.
     */
    public final TraceEventType type;

    /**
     * The parent of this event.
     */
    public final TraceEvent parent;

    /**
     * The parser being traced.
     */
    public final PetitParser parser;

    /**
     * The current parser context.
     */
    public final Context context;

    /**
     * The current invocation level.
     */
    public int getLevel() {
      return parent != null ? 1 + parent.getLevel() : 0;
    }

    private TraceEvent(TraceEventType type, TraceEvent parent, PetitParser parser, Context context) {
      this.type = type;
      this.parent = parent;
      this.parser = parser;
      this.context = context;
    }

    @Override
    public String toString() {
      var indent = Stream.generate(() -> "  ").limit(getLevel()).collect(Collectors.joining());
      return indent + (Objects.equals(TraceEventType.ENTER, type) ? parser : context);
    }
  }

}
