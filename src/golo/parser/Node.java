package golo.parser;

import golo.compiler.PositionInSourceCode;

// All AST nodes must implement this interface.
// It provides basic machinery for constructing the parent and child relationships between nodes.
public class Node {

  // This method is called after the Node has been made the current node.
  // It indicates that child nodes can now be added to it.
  public void open() {}

  // This method is called after all the child nodes have been added.
  public void close() {}

  // The parent of this Node
  public Node parent;

  // The children of this Node
  public Node[] children = new Node[0];

  public Object value; // not used

  public Token firstToken;
  public Token lastToken;

  /* You can override these two methods in subclasses of Node to
     customize the way the Node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

  public String toString() {
    return getClass().getSimpleName();
  }

}
