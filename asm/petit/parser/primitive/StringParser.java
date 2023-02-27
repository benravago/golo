package petit.parser.primitive;

import petit.parser.PetitParser;
import petit.parser.context.Context;
import petit.parser.context.Result;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Parses a sequence of characters.
 */
public class StringParser extends PetitParser {

  /**
   * Construct a parser that accepts the provides {@link String} {@code value}.
   */
  public static PetitParser of(String value) {
    return of(value, value + " expected");
  }

  /**
   * Construct a parser that accepts the provides {@link String} {@code value }, and that fails with the error provided error {@code message}.
   */
  public static PetitParser of(String value, String message) {
    return new StringParser(value.length(), value::equals, message);
  }

  /**
   * Construct a parser that accepts the provides {@link String} {@code value } case insensitive.
   */
  public static PetitParser ofIgnoringCase(String value) {
    return ofIgnoringCase(value, value + " expected");
  }

  /**
   * Construct a parser that accepts the provides {@link String} {@code value } case insensitive, and that fails with the error provided error {@code message}.
   */
  public static PetitParser ofIgnoringCase(String value, String message) {
    return new StringParser(value.length(), value::equalsIgnoreCase, message);
  }

  private final int size;
  private final Predicate<String> predicate;
  private final String message;

  private StringParser(int size, Predicate<String> predicate, String message) {
    this.size = size;
    this.predicate = Objects.requireNonNull(predicate, "Undefined predicate");
    this.message = Objects.requireNonNull(message, "Undefined message");
  }

  @Override
  public Result parseOn(Context context) {
    var buffer = context.getBuffer();
    var start = context.getPosition();
    var stop = start + size;
    if (stop <= buffer.length()) {
      var result = buffer.substring(start, stop);
      if (predicate.test(result)) {
        return context.success(result, stop);
      }
    }
    return context.failure(message);
  }

  @Override
  public int fastParseOn(String buffer, int position) {
    var stop = position + size;
    return stop <= buffer.length() && predicate.test(buffer.substring(position, stop)) ? stop : -1;
  }

  @Override
  protected boolean hasEqualProperties(PetitParser other) {
    return super.hasEqualProperties(other)
        && Objects.equals(size, ((StringParser) other).size)
        && Objects.equals(predicate, ((StringParser) other).predicate)
        && Objects.equals(message, ((StringParser) other).message);
  }

  @Override
  public StringParser copy() {
    return new StringParser(size, predicate, message);
  }

  @Override
  public String toString() {
    return super.toString() + "[" + message + "]";
  }

}
