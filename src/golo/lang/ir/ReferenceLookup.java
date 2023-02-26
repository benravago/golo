package golo.lang.ir;

/**
 * A reference lookup in the golo code.
 *
 * <p>This expression represents every reference use, that is when a variable is considered as an expression.
 */
public final class ReferenceLookup extends ExpressionStatement<ReferenceLookup> {

  private final String name;

  private ReferenceLookup(String name) {
    super();
    this.name = name;
  }

  /**
   * Creates a lookup to the reference defined by the given name.
   */
  public static ReferenceLookup of(Object name) {
    if (name instanceof ReferenceLookup) {
      return (ReferenceLookup) name;
    }
    if (name instanceof LocalReference) {
      return new ReferenceLookup(((LocalReference) name).getName());
    }
    return new ReferenceLookup(name.toString());
  }

  protected ReferenceLookup self() {
    return this;
  }

  public String getName() {
    return name;
  }

  /**
   * Resolves the reference in the given reference table.
   *
   * @param referenceTable the reference table in which to resolve the lookup.
   * @return the corresponding local reference node, or {@code null} if the reference does not exists in the table.
   */
  public LocalReference resolveIn(ReferenceTable referenceTable) {
    return referenceTable.get(name);
  }

  /**
   * Creates a corresponding local reference.
   * <p>
   * The created local reference is a variable one, with the same name as this lookup.
   *
   * @return a new variable local reference.
   */
  public LocalReference varRef() {
    return LocalReference.of(name).variable();
  }

  /**
   * Creates a corresponding local reference.
   * <p>
   * The created local reference is an immutable one, with the same name as this lookup.
   *
   * @return a new immutable local reference.
   */
  public LocalReference letRef() {
    return LocalReference.of(name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return String.format("Ref{name=%s}", getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitReferenceLookup(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }
}
