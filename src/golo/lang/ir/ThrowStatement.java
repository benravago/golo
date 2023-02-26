package golo.lang.ir;

import java.util.Objects;
import java.util.Collections;
import java.util.List;

public final class ThrowStatement extends GoloStatement<ThrowStatement> {

  private GoloStatement<?> expressionStatement;

  private ThrowStatement(GoloStatement<?> expressionStatement) {
    super();
    setExpressionStatement(expressionStatement);
  }

  /**
   * Creates a {@code throw} statement.
   *
   * @param expression an object interpreted as an {@link ExpressionStatement} that will evaluate to an exception.
   * @see ExpressionStatement#of(Object)
   */
  public static ThrowStatement of(Object expression) {
    return new ThrowStatement(ExpressionStatement.of(expression));
  }

  protected ThrowStatement self() {
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitThrowStatement(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GoloElement<?>> children() {
    return Collections.singletonList(expressionStatement);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void replaceElement(GoloElement<?> original, GoloElement<?> newElement) {
    if (Objects.equals(original, expressionStatement) && newElement instanceof ExpressionStatement) {
      setExpressionStatement(ExpressionStatement.of(newElement));
    } else {
      throw cantReplace(original, newElement);
    }
  }

  public GoloStatement<?> expression() {
    return expressionStatement;
  }

  private void setExpressionStatement(GoloStatement<?> stat) {
    this.expressionStatement = makeParentOf(stat);
  }
}
