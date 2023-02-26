package golo.lang.ir;

public interface BlockContainer<T> {
  Block getBlock();

  /**
   * Defines the contained block.
   *
   * <p>This is a builder method.
   *
   * @param block an object that can be converted into a {@link Block}
   * @see Block#of(Object)
   */
  T block(Object block);

  /**
   * Defines the block as the given statements.
   * <p>This is a builder method.
   * @param statements the statements to execute.
   */
  default T body(Object... statements) {
    return this.block(Block.block(statements));
  }
}
