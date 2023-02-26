package golo.lang.concurrent.workers;

/**
 * A worker function for asynchronously processing messages.
 * <p>
 * This interface is mostly used to facilitate the design of the Java API, as worker functions are made out of
 * function references in Golo.
 */
@FunctionalInterface
public interface WorkerFunction {

  /**
   * Called by a worker executor to process a message.
   *
   * @param message the message to process.
   */
  void apply(Object message);
}
