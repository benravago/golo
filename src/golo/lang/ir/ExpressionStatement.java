package golo.lang.ir;

import java.util.LinkedList;

public abstract class ExpressionStatement<T extends ExpressionStatement<T>> extends GoloStatement<T> {

  private final LinkedList<GoloAssignment<?>> declarations = new LinkedList<>();

  /**
   * Defines a variable declaration (assignment) local to this expression.
   */
  public T with(Object a) {
    if (!(a instanceof GoloAssignment)) {
      throw new IllegalArgumentException(("Must be an assignment, got " + a));
    }
    GoloAssignment<?> declaration = (GoloAssignment<?>) a;
    declarations.add(declaration.declaring());
    makeParentOf(declaration);
    return self();
  }

  /**
   * Returns the local declarations of this expression if any.
   */
  public GoloAssignment<?>[] declarations() {
    return declarations.toArray(new GoloAssignment<?>[declarations.size()]);
  }

  /**
   * Checks if this expression has local variable declarations.
   */
  public boolean hasLocalDeclarations() {
    return !declarations.isEmpty();
  }

  /**
   * Removes all local declarations.
   */
  public void clearDeclarations() {
    declarations.clear();
  }

  /**
   * Creates an binary operation representing the anonymous call on this expression.
   *
   * <p>For instance, code such as:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * (|x| -> x + 2)(40)
   * </code></pre>
   * actually creates a {@link BinaryOperation} or type {@link OperatorType#ANON_CALL}
   * whose first argument is a {@code ClosureReference} and the second one a {@link FunctionInvocation} holding the
   * argument.
   *
   * <p>This method ease the creation of such a node.
   * This expression must return a {@link golo.lang.FunctionReference} when evaluated. This is <em>not checked</em>.
   *
   * <p>For instance, in code like
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * let f = |x| -> |y| -> x * y
   * f(2)(42)
   * </code></pre>
   * the call is an anonymous call between {@code f(2)} and {@code (42)}, and can be generated by:
   * <pre class="listing"><code class="lang-java" data-lang="java">
   * call("f").withArgs(constant(2)).call(constant(42))
   * </pre></code>
   * <p>See also the
   * <a href="http://golo-lang.org/documentation/next/#_calling_functions_that_return_functions">
   * Golo guide</a>.
   */
  public BinaryOperation call(Object... arguments) {
    FunctionInvocation invocation;
    if (arguments.length == 1 && arguments[0] instanceof FunctionInvocation) {
      invocation = (FunctionInvocation) arguments[0];
      if (!invocation.isAnonymous()) {
        throw new IllegalArgumentException("Invocation in anonymous calls must be anonymous.");
      }
    } else {
      invocation = FunctionInvocation.of(null).withArgs(arguments);
    }
    return BinaryOperation.create(OperatorType.ANON_CALL, this, invocation);
  }

  /**
   * Expression coercion.
   *
   * <p>If the given value is an expression, casts it. If it's a literal value, returns a {@code ConstantStatement}.
   * If it's a {@link LocalReference}, creates a {@link ReferenceLookup} from it.
   *
   * @see ConstantStatement#isLiteralValue(Object)
   */
  public static ExpressionStatement<?> of(Object expr) {
    if (expr instanceof ExpressionStatement) {
      return (ExpressionStatement<?>) expr;
    }
    if (ConstantStatement.isLiteralValue(expr)) {
      return ConstantStatement.of(expr);
    }
    if (expr instanceof LocalReference) {
      return ((LocalReference) expr).lookup();
    }
    throw cantConvert("ExpressionStatement", expr);
  }
}
