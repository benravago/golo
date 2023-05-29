package golo.parser.ast;

import java.util.List;

public class Function extends golo.parser.Node {
  public List<String> parameters;
  public boolean isVarargs;
  public boolean isCompactForm;
}
