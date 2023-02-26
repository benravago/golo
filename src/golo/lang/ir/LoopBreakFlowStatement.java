package golo.lang.ir;

public final class LoopBreakFlowStatement extends GoloStatement<LoopBreakFlowStatement> {

  public enum Type {
    BREAK, CONTINUE
  }

  private final Type type;

  private LoopBreakFlowStatement(Type type) {
    super();
    this.type = type;
  }

  public static LoopBreakFlowStatement newContinue() {
    return new LoopBreakFlowStatement(Type.CONTINUE);
  }

  public static LoopBreakFlowStatement newBreak() {
    return new LoopBreakFlowStatement(Type.BREAK);
  }

  protected LoopBreakFlowStatement self() {
    return this;
  }

  public Type getType() {
    return type;
  }

  public LoopStatement getEnclosingLoop() {
    return ancestorOfType(LoopStatement.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitLoopBreakFlowStatement(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    throw cantReplace();
  }

}
