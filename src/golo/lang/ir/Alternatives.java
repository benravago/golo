package golo.lang.ir;

import java.util.List;

/**
 * Alternative nodes, such as `match` or `case`.
 * <p> This interface is mainly a fluent builder for a {@link WhenClause} container.
 */
public interface Alternatives<T extends GoloElement<?>> {

  /**
   * Defines a new alternative clause.
   *
   * <p>This is a builder method.
   *
   * @param cond an expression node defining the clause.
   */
  Alternatives<T> when(Object cond);

  /**
   * Defines the action of the previous clause.
   *
   * <p>This is a builder method.</p>
   *
   * <p>It <em>must</em> be called after a call to {@code when}. For instance
   * <pre class="listing"><code class="lang-java" data-lang="java">
   *  alternative.when(condition).then(action);
   * </code></pre>
   *
   * @param action the action to execute in this clause. Its type depends on the kind of alternative
   * (i.e. the {@link WhenClause} constructed)
   */
  Alternatives<T> then(Object action);

  /**
   * Defines the default alternative clause.
   *
   * <p>This is a builder method.
   *
   * @param action the action to execute in this clause. Its type depends on the kind of alternative
   * (i.e. the {@link WhenClause} constructed)
   */
  Alternatives<T> otherwise(Object action);

  List<WhenClause<T>> getClauses();

  T getOtherwise();
}
