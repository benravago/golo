package org.objectweb.asm;

public class Opcodes {

  public static final int
    V1_8 = 52;
  
  public static final int
    ACC_PUBLIC     = 0x0001,
    ACC_PRIVATE    = 0x0002,
    ACC_PROTECTED  = 0x0004,
    ACC_STATIC     = 0x0008,
    ACC_FINAL      = 0x0010,
    ACC_SUPER      = 0x0020,
    ACC_VARARGS    = 0x0080,
    ACC_ABSTRACT   = 0x0400,
    ACC_SYNTHETIC  = 0x1000,
    ACC_DEPRECATED = 0x020000; // 131072;
  
  public static final int
    H_INVOKESTATIC = 6;
 
  public static final int
    ACONST_NULL    =   1,
    ICONST_M1      =   2,
    ICONST_0       =   3,
    ICONST_1       =   4,
    ICONST_2       =   5,
    ICONST_3       =   6,
    ICONST_4       =   7,
    ICONST_5       =   8,
    LCONST_0       =   9,
    LCONST_1       =  10,
    BIPUSH         =  16,
    SIPUSH         =  17,
    ILOAD          =  21,
    LLOAD          =  22,
    FLOAD          =  23,
    DLOAD          =  24,
    ALOAD          =  25,
    ASTORE         =  58,
    AASTORE        =  83,
    POP            =  87,
    DUP            =  89,
    IFEQ           = 153,
    IFNE           = 154,
    IF_ACMPNE      = 166,
    GOTO           = 167,
    IRETURN        = 172,
    LRETURN        = 173,
    FRETURN        = 174,
    DRETURN        = 175,  
    ARETURN        = 176,
    RETURN         = 177,
    GETSTATIC      = 178,
    PUTSTATIC      = 179,
    GETFIELD       = 180,
    PUTFIELD       = 181,
    INVOKEVIRTUAL  = 182,
    INVOKESPECIAL  = 183,
    INVOKESTATIC   = 184,
    NEW            = 187,
    ANEWARRAY      = 189,
    ATHROW         = 191,
    CHECKCAST      = 192,
    INSTANCEOF     = 193,
    IFNULL         = 198,
    IFNONNULL      = 199;
}
