package golo.parser.ast;

import java.util.List;

public class ImportDeclaration extends golo.parser.Node {
  public String name;
  public boolean isRelative;
  public List<String> multiples;
}
