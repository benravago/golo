package golo.runtime.adapters;

public class AdapterDefinitionProblem extends RuntimeException {

  public AdapterDefinitionProblem(String message) {
    super(message);
  }

  public AdapterDefinitionProblem(Throwable cause) {
    super(cause);
  }

  public AdapterDefinitionProblem(String message, Throwable cause) {
    super(message, cause);
  }
}
