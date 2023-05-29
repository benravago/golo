package org.ow2.asm;
public class Handle {

  int tag;
  String owner;
  String name;
  String descriptor;
  boolean isInterface;

  public Handle(int tag, String owner, String name, String descriptor, boolean isInterface) {
    this.tag = tag;
    this.owner = owner;
    this.name = name;
    this.descriptor = descriptor;
    this.isInterface = isInterface;
  }
}
