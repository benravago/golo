package golo.lang.ir;

/**
 * A reference to a class.
 *
 * <p>Used to represent a literal class notation.
 */
public final class ClassReference {
  private final String name;

  private ClassReference(String name) {
    this.name = java.util.Objects.requireNonNull(name);
  }

  public static ClassReference of(Object o) {
    if (o instanceof ClassReference) {
      return (ClassReference) o;
    }
    if (o instanceof Class<?>) {
      return new ClassReference(((Class<?>) o).getCanonicalName());
    }
    return new ClassReference(o.toString());
  }

  public String getName() {
    return this.name;
  }

  public String toJVMType() {
    return this.name.replaceAll("\\.", "#");
  }

  public Class<?> dereference() throws ClassNotFoundException {
    return Class.forName(this.name, true, golo.lang.Runtime.classLoader());
  }

  @Override
  public String toString() {
    return "Class<" + name + ">";
  }
}
