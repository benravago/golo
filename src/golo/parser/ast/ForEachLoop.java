package golo.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class ForEachLoop extends golo.parser.Node {
  public String elementIdentifier;
  public List<String> names = new ArrayList<>();
  public boolean isVarargs;  
}
