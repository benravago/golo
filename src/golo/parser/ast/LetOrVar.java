package golo.parser.ast;

public class LetOrVar extends golo.parser.Node {
  public Type type;
  public String name;
  public boolean isModule;

  public enum Type {
    LET, VAR
  }
}
