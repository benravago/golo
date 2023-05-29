package org.ow2.asm;

class Edge {

  static final int JUMP = 0;
  static final int EXCEPTION = 0x7FFFFFFF;

  int info;
  Label successor;

  Edge nextEdge;

  Edge(int info, Label successor, Edge nextEdge) {
    this.info = info;
    this.successor = successor;
    this.nextEdge = nextEdge;
  }
}
