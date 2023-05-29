package org.ow2.asm;

class Constants { // implements Opcodes // TODO: prune unused

  static final String
    INNER_CLASSES = "InnerClasses",
    ENCLOSING_METHOD = "EnclosingMethod",
    SIGNATURE = "Signature",
    SOURCE_FILE = "SourceFile",
    LINE_NUMBER_TABLE = "LineNumberTable",
    LOCAL_VARIABLE_TABLE = "LocalVariableTable",
    LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable",
    RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations",
    RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations",
    BOOTSTRAP_METHODS = "BootstrapMethods",
    NEST_HOST = "NestHost",
    NEST_MEMBERS = "NestMembers";
 
  static final String
    CONSTANT_VALUE = "ConstantValue",
    CODE = "Code",
    STACK_MAP = "StackMap",
    STACK_MAP_TABLE = "StackMapTable",
    EXCEPTIONS = "Exceptions",
    DEPRECATED = "Deprecated", 
    ANNOTATION_DEFAULT = "AnnotationDefault",
    METHOD_PARAMETERS = "MethodParameters";

  static final int 
    ACC_CONSTRUCTOR = 0x40000; // method access flag.

  static final int 
    F_INSERT = 256;

  static final int 
    LDC_W = 19,
    LDC2_W = 20,
    ILOAD_0 = 26,
    ILOAD_1 = 27,
    ILOAD_2 = 28,
    ILOAD_3 = 29,
    LLOAD_0 = 30,
    LLOAD_1 = 31,
    LLOAD_2 = 32,
    LLOAD_3 = 33,
    FLOAD_0 = 34,
    FLOAD_1 = 35,
    FLOAD_2 = 36,
    FLOAD_3 = 37,
    DLOAD_0 = 38,
    DLOAD_1 = 39,
    DLOAD_2 = 40,
    DLOAD_3 = 41,
    ALOAD_0 = 42,
    ALOAD_1 = 43,
    ALOAD_2 = 44,
    ALOAD_3 = 45,
    ISTORE_0 = 59,
    ISTORE_1 = 60,
    ISTORE_2 = 61,
    ISTORE_3 = 62,
    LSTORE_0 = 63,
    LSTORE_1 = 64,
    LSTORE_2 = 65,
    LSTORE_3 = 66,
    FSTORE_0 = 67,
    FSTORE_1 = 68,
    FSTORE_2 = 69,
    FSTORE_3 = 70,
    DSTORE_0 = 71,
    DSTORE_1 = 72,
    DSTORE_2 = 73,
    DSTORE_3 = 74,
    ASTORE_0 = 75,
    ASTORE_1 = 76,
    ASTORE_2 = 77,
    ASTORE_3 = 78,
    WIDE = 196,
    GOTO_W = 200,
    JSR_W = 201;

  static final int 
    WIDE_JUMP_OPCODE_DELTA = GOTO_W - Opcodes.GOTO;

}
