package golo.lang.ir;

import static java.util.Objects.requireNonNull;

/**
 * Empty IR node.
 */
public final class Noop extends GoloStatement<Noop> implements ToplevelGoloElement {

  private final String comment;

  private Noop(String comment) {
    this.comment = requireNonNull(comment);
  }

  public static Noop of(Object comment) {
    return new Noop(comment == null ? "" : comment.toString());
  }

  protected Noop self() {
    return this;
  }

  public String comment() {
    return this.comment;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitNoop(this);
  }

  @Override
  public void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }
}
