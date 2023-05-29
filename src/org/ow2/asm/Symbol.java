package org.ow2.asm;

class Symbol {

  static final int
    CONSTANT_CLASS_TAG = 7,
    CONSTANT_FIELDREF_TAG = 9,
    CONSTANT_METHODREF_TAG = 10,
    CONSTANT_INTERFACE_METHODREF_TAG = 11,
    CONSTANT_STRING_TAG = 8,
    CONSTANT_INTEGER_TAG = 3,
    CONSTANT_FLOAT_TAG = 4,
    CONSTANT_LONG_TAG = 5,
    CONSTANT_DOUBLE_TAG = 6,
    CONSTANT_NAME_AND_TYPE_TAG = 12,
    CONSTANT_UTF8_TAG = 1,
    CONSTANT_METHOD_HANDLE_TAG = 15,
    CONSTANT_METHOD_TYPE_TAG = 16,
    CONSTANT_DYNAMIC_TAG = 17,
    CONSTANT_INVOKE_DYNAMIC_TAG = 18,
    CONSTANT_MODULE_TAG = 19,
    CONSTANT_PACKAGE_TAG = 20,
    BOOTSTRAP_METHOD_TAG = 64,
    TYPE_TAG = 128,
    UNINITIALIZED_TYPE_TAG = 129,
    MERGED_TYPE_TAG = 130;

  int index;
  int tag;
  String owner;
  String name;
  String value;
  long data;

  int info;

  Symbol(int index, int tag, String owner, String name, String value, long data) {
    this.index = index;
    this.tag = tag;
    this.owner = owner;
    this.name = name;
    this.value = value;
    this.data = data;
  }

  int getArgumentsAndReturnSizes() {
    if (info == 0) {
      info = Type.getArgumentsAndReturnSizes(value);
    }
    return info;
  }
}
