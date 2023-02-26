package golo.lang.ir;

import java.util.Collections;
import java.util.List;

public final class UnaryOperation extends ExpressionStatement<UnaryOperation> {

  private final OperatorType type;
  private ExpressionStatement<?> expressionStatement;

  UnaryOperation(OperatorType type, ExpressionStatement<?> expressionStatement) {
    super();
    this.type = type;
    setExpressionStatement(expressionStatement);
  }

  /**
   * Creates a generic unary operation.
   */
  public static UnaryOperation create(Object type, Object expression) {
    return new UnaryOperation(OperatorType.of(type), ExpressionStatement.of(expression));
  }

  protected UnaryOperation self() {
    return this;
  }

  public ExpressionStatement<?> expression() {
    return expressionStatement;
  }

  private void setExpressionStatement(ExpressionStatement<?> statement) {
    this.expressionStatement = makeParentOf(statement);
  }

  public OperatorType getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitUnaryOperation(this);
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
    if (expressionStatement.equals(original)) {
      setExpressionStatement(ExpressionStatement.of(newElement));
    } else {
      throw cantReplace(original, newElement);
    }
  }
}
