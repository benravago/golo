package golo.parser;

import golo.compiler.PositionInSourceCode;

public class SimpleNode {

  // public List<SimpleNode> children; 

  public void addChild(SimpleNode n, int i) {
  } // children.add(i,n)

  public SimpleNode getChild(int i) {
    return null;
  } // children.get(i)

  public int getNumChildren() {
    return -1;
  } // children.size()

  public Object accept(GoloParserVisitor visitor, Object data) {
    return null;
  }

  public Object childrenAccept(GoloParserVisitor visitor, Object data) {
    return null;
  }

  public String toString(String prefix) {
    return null;
  }

  public void dump(String prefix) {
  }

  // other functions

  public PositionInSourceCode getPositionInSourceCode() {
    return null;
  }

  public String getDocumentation() {
    return null;
  }

}
