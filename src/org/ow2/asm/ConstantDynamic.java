package org.ow2.asm;
class ConstantDynamic {

  String name;
  String descriptor;
  Handle bootstrapMethod;
  Object[] bootstrapMethodArguments;

  ConstantDynamic(String name, String descriptor, Handle bootstrapMethod, Object... bootstrapMethodArguments) {
    this.name = name;
    this.descriptor = descriptor;
    this.bootstrapMethod = bootstrapMethod;
    this.bootstrapMethodArguments = bootstrapMethodArguments;
  }
}
