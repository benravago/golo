package golo.parser;

public interface Nodes {

  // map of getters per class
  static ClassValue<?> documented = null; // TODO
  static ClassValue<?> named = null; // TODO

  // https://stackoverflow.com/questions/7444420/classvalue-in-java-7
  // https://stackoverflow.com/questions/34069386/methodhandle-to-a-getter-setter-from-another-class-gives-a-nosuchfielderror:wq

  // use MethodHandles to get field value

  static String getName(Object node) {
    // 1. test if node has 'name' field
    // 2. invoke named.lookup().get()
    return null;
  }

  static String getDocumentation(Object node) {
    return null;
  }

  /** This method tells the Node to add a child to the its list of children.  */
  static void set(Node p, Node c, int i) {
    if (p.children == null) {
      p.children = new Node[i + 1];
    } else if (i >= p.children.length) {
      var n = new Node[i + 1];
      System.arraycopy(p.children, 0, n, 0, p.children.length);
      p.children = n;
    }
    p.children[i] = c;
  }

  static String toString(Node n, String prefix) {
    return null; // TODO:
  }

}
/*

  ** Override this method if you want to customize how the Node dumps out its children. **
  void dump(String prefix) {
    System.out.println(toString(prefix));
    if (children != null) for (var n:children) if (n != null) n.dump(prefix + " ");
  }

  String toString(String prefix) {
    return prefix + toString();
  }

*/
