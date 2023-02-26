package golo.lang.ir;

public abstract class GoloStatement<T extends GoloStatement<T>> extends GoloElement<T> {

  /**
   * Statement coercion.
   *
   * <p>If the given value is an statement, cast it. If it's {@code null} returns a {@code Noop}.
   */
  public static GoloStatement<?> of(Object statement) {
    if (statement == null) {
      return Noop.of("null statement");
    }
    if (statement instanceof GoloStatement) {
      return (GoloStatement) statement;
    }
    throw cantConvert("GoloStatement", statement);
  }
}
