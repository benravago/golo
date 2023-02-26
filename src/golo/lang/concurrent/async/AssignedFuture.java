package golo.lang.concurrent.async;

/**
 * Convenience implementation for pre-set futures.
 */
public final class AssignedFuture implements Future {

  private AssignedFuture(Object value) {
    this.value = value;
  }

  private final Object value;

  /**
   * Builds a new future that has been set to a value.
   *
   * @param value the future value.
   * @return a new future object.
   */
  public static AssignedFuture setFuture(Object value) {
    return new AssignedFuture(value);
  }

  /**
   * Builds a new future that has failed.
   *
   * @param throwable the failure.
   * @return a new future object.
   */
  public static AssignedFuture failedFuture(Throwable throwable) {
    return new AssignedFuture(throwable);
  }

  @Override
  public Object get() {
    return value;
  }

  @Override
  public Object blockingGet() throws InterruptedException {
    return value;
  }

  @Override
  public boolean isResolved() {
    return true;
  }

  @Override
  public boolean isFailed() {
    return value instanceof Throwable;
  }

  @Override
  public Future onSet(Observer observer) {
    if (!isFailed()) {
      observer.apply(value);
    }
    return this;
  }

  @Override
  public Future onFail(Observer observer) {
    if (isFailed()) {
      observer.apply(value);
    }
    return this;
  }

  @Override
  public String toString() {
    return String.format("AssignedFuture{value=%s}", value);
  }
}
