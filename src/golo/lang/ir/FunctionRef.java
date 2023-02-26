package golo.lang.ir;

/**
 * A literal reference to a function.
 *
 * <p>Used to represent a literal function reference notation, such as
 * {@code ^my.package::foo}
 */
public final class FunctionRef {

  private final String module;
  private final String name;
  private final int arity;
  private final boolean varargs;

  private FunctionRef(String module, String name, int arity, boolean varargs) {
    this.module = module;
    this.name = name;
    this.arity = arity;
    this.varargs = varargs;
  }

  public static FunctionRef of(String module, String name, int arity, boolean varargs) {
    return new FunctionRef(module, name, arity, varargs);
  }

  public String module() {
    return this.module;
  }

  public String name() {
    return this.name;
  }

  public int arity() {
    return this.arity;
  }

  public boolean varargs() {
    return this.varargs;
  }

  @Override
  public String toString() {
    return String.format("FunctionRef{module=%s,name=%s,arity=%s%s}", module, name, arity, (varargs ? ",varargs" : ""));
  }
}
