package golo.lang.ir;

/**
 * Represent a method invocation on an expression.
 * <p>
 * For instance golo code as
 * <pre class="listing"><code class="lang-golo" data-lang="golo">
 * foo()?: bar(42)
 * </code></pre>
 * can be generated with:
   * <pre class="listing"><code class="lang-java" data-lang="java">
 * invoke("bar").nullSafe().withArgs(42).on(call("foo"))
 * </code></pre>
 */
public final class MethodInvocation extends AbstractInvocation<MethodInvocation> {

  private boolean nullSafeGuarded = false;

  private MethodInvocation(String name) {
    super(name);
  }

  public static MethodInvocation invoke(String name) {
    return new MethodInvocation(name);
  }

  protected MethodInvocation self() {
    return this;
  }

  /**
   * Checks if this invocation is null-safe.
   * <p>
   * I.e. if called with an elvis operator syntax ({@code obj?: method()})
   */
  public boolean isNullSafeGuarded() {
    return nullSafeGuarded;
  }

  /**
   * Defines if the invocation is null-safe or not.
   */
  public MethodInvocation nullSafe(boolean v) {
    this.nullSafeGuarded = v;
    return this;
  }

  /**
   * Defines the invocation as null-safe.
   */
  public MethodInvocation nullSafe() {
    return nullSafe(true);
  }

  /**
   * Defines the receiver of the invocation.
   */
  public BinaryOperation on(Object target) {
    return BinaryOperation.of(nullSafeGuarded ? OperatorType.ELVIS_METHOD_CALL : OperatorType.METHOD_CALL)
        .left(ExpressionStatement.of(target)).right(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MethodInvocation withArgs(Object... args) {
    super.withArgs(args);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitMethodInvocation(this);
  }
}
