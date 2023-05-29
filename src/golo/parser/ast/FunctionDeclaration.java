package golo.parser.ast;

public class FunctionDeclaration extends golo.parser.Node {
  public String name;
  public boolean isLocal;
  public boolean isAugmentation;
  public boolean isDecorator;
  public boolean isMacro;
  public String documentation;  
}
