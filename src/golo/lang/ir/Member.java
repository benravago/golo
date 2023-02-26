package golo.lang.ir;

import static java.util.Objects.requireNonNull;

public final class Member extends GoloElement<Member> {

  private final String name;

  private Member(String name) {
    super();
    this.name = requireNonNull(name);
  }

  public static Member of(Object o) {
    if (o instanceof Member) {
      return (Member) o;
    }
    return new Member(o.toString());
  }

  protected Member self() {
    return this;
  }

  public String getName() {
    return name;
  }

  public boolean isPublic() {
    return !name.startsWith("_");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitMember(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace(original, newElement);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return String.format("<%s>", name);
  }
}
