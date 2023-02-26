package golo.parser.ast;

import golo.parser.NamedNode;

public class LetOrVar extends NamedNode {

  public enum Type {
    LET, VAR
  }

  public Type getType() {
    return null;
  }

  public void setType(Type type) {
  }

  public boolean isModuleState() {
    return false;
  }

  public void setModuleState(boolean moduleState) {
  }

}
