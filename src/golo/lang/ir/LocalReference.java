package golo.lang.ir;

import golo.compiler.SymbolGenerator;

/**
 * Represents a reference.
 *
 * <p>A reference is ether a variable (as defined by <code class="lang-golo">var answer = 42</code>) or a constant
 * (as defined by <code class="lang-golo">let answer = 42</code>).
 */
public final class LocalReference extends GoloElement<LocalReference> {

  private static final SymbolGenerator REF_SYMBOLS = new SymbolGenerator("golo.ir.builders.ref");

  public enum Kind {
    CONSTANT, VARIABLE, MODULE_CONSTANT, MODULE_VARIABLE
  }

  private Kind kind = Kind.CONSTANT;
  private final String name;
  private boolean synthetic = false;
  private int index = -1;

  private LocalReference(String name) {
    super();
    this.name = name;
  }

  /**
   * Creates a local reference.
   *
   * <p>If the argument is already a local reference, it is returned unchanged, otherwise, its
   * string representation is used to name the reference.
   *
   * @param name the name of the reference
   * @return a local reference to the variable named after the argument
   */
  public static LocalReference of(Object name) {
    if (name instanceof LocalReference) {
      return (LocalReference) name;
    }
    if (name instanceof ReferenceLookup) {
      return new LocalReference(((ReferenceLookup) name).getName());
    }
    return new LocalReference(name.toString());
  }

  /**
   * Creates a new local reference with a generated unique name.
   */
  public static LocalReference generate() {
    return new LocalReference(REF_SYMBOLS.next());
  }

  public static LocalReference generate(String prefix) {
    return new LocalReference(REF_SYMBOLS.next(prefix));
  }

  public static LocalReference create(Object name, Kind kind) {
    return of(name).kind(kind);
  }

  protected LocalReference self() {
    return this;
  }

  public Kind getKind() {
    return kind;
  }

  public LocalReference variable() {
    if (kind == Kind.MODULE_VARIABLE || kind == Kind.MODULE_CONSTANT) {
      kind = Kind.MODULE_VARIABLE;
    } else {
      kind = Kind.VARIABLE;
    }
    return this;
  }

  public LocalReference moduleLevel() {
    if (kind == Kind.CONSTANT || kind == Kind.MODULE_CONSTANT) {
      kind = Kind.MODULE_CONSTANT;
    } else {
      kind = Kind.MODULE_VARIABLE;
    }
    return this;
  }

  public LocalReference kind(Kind k) {
    kind = k;
    return this;
  }

  public String getName() {
    return name;
  }

  public LocalReference synthetic(boolean isSynthetic) {
    this.synthetic = isSynthetic;
    return this;
  }

  public LocalReference synthetic() {
    return synthetic(true);
  }

  public boolean isSynthetic() {
    return synthetic;
  }

  public boolean isModuleState() {
    return kind == Kind.MODULE_CONSTANT || kind == Kind.MODULE_VARIABLE;
  }

  public boolean isConstant() {
    return kind == Kind.CONSTANT || kind == Kind.MODULE_CONSTANT;
  }

  /**
   * Internal API
   */
  public int getIndex() {
    return index;
  }

  /**
   * Internal API
   */
  public void setIndex(int index) {
    this.index = index;
  }

  public LocalReference index(int index) {
    setIndex(index);
    return this;
  }

  /**
   * Returns a {@link ReferenceLookup} referencing this variable.
   */
  public ReferenceLookup lookup() {
    return ReferenceLookup.of(name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return String.format("LocalReference{kind=%s, name='%s', index=%d}", kind, name, index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocalReference that = (LocalReference) o;
    return kind == that.kind && name.equals(that.name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int result = kind.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitLocalReference(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }
}
