package golo.compiler;

import java.util.Arrays;

/**
 * A code generation result.
 * <p>
 * Compiling a single Golo source file may result in several JVM classes to be produced.
 * A <code>CodeGenerationResult</code> represents one such output.
 */
public final class CodeGenerationResult {

  private final byte[] bytecode;
  private final PackageAndClass packageAndClass;
  private final String sourceFile;

  /**
   * Constructor for a code generation result.
   *
   * @param bytecode        the JVM bytecode as an array.
   * @param packageAndClass the package and class descriptor for the bytecode.
   */
  public CodeGenerationResult(byte[] bytecode, PackageAndClass packageAndClass, String sourceFile) {
    if (bytecode == null) {
      this.bytecode = new byte[0];
    } else {
      this.bytecode = Arrays.copyOf(bytecode, bytecode.length);
    }
    this.packageAndClass = packageAndClass;
    this.sourceFile = sourceFile;
  }

  /**
   * @return the bytecode array.
   */
  public byte[] getBytecode() {
    return Arrays.copyOf(this.bytecode, this.bytecode.length);
  }

  /**
   * @return the package and class descriptor.
   */
  public PackageAndClass getPackageAndClass() {
    return packageAndClass;
  }

  /**
   * @return the binary name of the described class
   */
  public String getBinaryName() {
    return packageAndClass.toString();
  }

  /**
   * @return the relative filename of the corresponding class.
   */
  public String getOutputFilename() {
    return packageAndClass.getFilename();
  }

  public String getSourceFilename() {
    return this.sourceFile;
  }

  public int size() {
    return this.bytecode.length;
  }

  @Override
  public String toString() {
    return String.format("CodeGenerationResult{name=%s, src=%s}", getPackageAndClass().toString(), getSourceFilename());
  }
}
