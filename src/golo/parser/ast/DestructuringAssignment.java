package golo.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class DestructuringAssignment extends golo.parser.Node {
  public LetOrVar.Type type;
  public List<String> names = new ArrayList<>();
  public boolean isVarargs;
}
