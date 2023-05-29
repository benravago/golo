package org.ow2.asm;
public class ClassWriter {

  public static final int COMPUTE_MAXS = 1;
  public static final int COMPUTE_FRAMES = 2;

  int version;
  SymbolTable symbolTable;
  int accessFlags;
  int thisClass;
  int superClass;
  int interfaceCount;
  int[] interfaces;
  FieldWriter firstField;
  FieldWriter lastField;
  MethodWriter firstMethod;
  MethodWriter lastMethod;
  int numberOfInnerClasses;
  ByteVector innerClasses;
  int enclosingClassIndex;
  int enclosingMethodIndex;
  int signatureIndex;
  int sourceFileIndex;
  AnnotationWriter lastRuntimeVisibleAnnotation;
  AnnotationWriter lastRuntimeInvisibleAnnotation;

  public ClassWriter(int flags) { // flags
    // super(null);
    symbolTable = new SymbolTable(this);
  }
  /*
  public ClassWriter(final ClassReader classReader, final int flags) {
    super(** latest api = ** Opcodes.ASM9);
    symbolTable = classReader == null ? new SymbolTable(this) : new SymbolTable(this, classReader);
    if ((flags & COMPUTE_FRAMES) != 0) {
      this.compute = MethodWriter.COMPUTE_ALL_FRAMES;
    } else if ((flags & COMPUTE_MAXS) != 0) {
      this.compute = MethodWriter.COMPUTE_MAX_STACK_AND_LOCAL;
    } else {
      this.compute = MethodWriter.COMPUTE_NOTHING;
    }
  } 
  */

  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    this.version = version;
    this.accessFlags = access;
    this.thisClass = symbolTable.setMajorVersionAndClassName(version & 0xFFFF, name);
    if (signature != null) {
      this.signatureIndex = symbolTable.addConstantUtf8(signature);
    }
    this.superClass = superName == null ? 0 : symbolTable.addConstantClass(superName).index;
    if (interfaces != null && interfaces.length > 0) {
      interfaceCount = interfaces.length;
      this.interfaces = new int[interfaceCount];
      for (var i = 0; i < interfaceCount; ++i) {
        this.interfaces[i] = symbolTable.addConstantClass(interfaces[i]).index;
      }
    }
  }

  public void visitSource(String file, String debug) {
    if (file != null) {
      sourceFileIndex = symbolTable.addConstantUtf8(file);
    }
  }

  public void visitOuterClass(String owner, String name, String descriptor) {
    enclosingClassIndex = symbolTable.addConstantClass(owner).index;
    if (name != null && descriptor != null) {
      enclosingMethodIndex = symbolTable.addConstantNameAndType(name, descriptor);
    }
  }

  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    return visible 
      ? (lastRuntimeVisibleAnnotation = AnnotationWriter.create(symbolTable, descriptor, lastRuntimeVisibleAnnotation))
      : (lastRuntimeInvisibleAnnotation = AnnotationWriter.create(symbolTable, descriptor, lastRuntimeInvisibleAnnotation));
  }

  public void visitInnerClass(String name, String outerName, String innerName, int access) {
    if (innerClasses == null) {
      innerClasses = new ByteVector();
    }
    var nameSymbol = symbolTable.addConstantClass(name);
    if (nameSymbol.info == 0) {
      ++numberOfInnerClasses;
      innerClasses.putShort(nameSymbol.index);
      innerClasses.putShort(outerName == null ? 0 : symbolTable.addConstantClass(outerName).index);
      innerClasses.putShort(innerName == null ? 0 : symbolTable.addConstantUtf8(innerName));
      innerClasses.putShort(access);
      nameSymbol.info = numberOfInnerClasses;
    }
  }
  
  public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
    var fieldWriter = new FieldWriter(symbolTable, access, name, descriptor, signature, value);
    if (firstField == null) {
      firstField = fieldWriter;
    } else {
      lastField.fv = fieldWriter;
    }
    return lastField = fieldWriter;
  }

  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
    var methodWriter = new MethodWriter(symbolTable, access, name, descriptor, signature, exceptions); // MethodWriter.COMPUTE_ALL_FRAMES
    if (firstMethod == null) {
      firstMethod = methodWriter;
    } else {
      lastMethod.mv = methodWriter;
    }
    return lastMethod = methodWriter;
  }

  public void visitEnd() {
    // Nothing to do.
  }

  String getCommonSuperClass(String type1, String type2) {
    var classLoader = getClass().getClassLoader();;
    Class<?> class1;
    try {
      class1 = Class.forName(type1.replace('/', '.'), false, classLoader);
    } catch (ClassNotFoundException e) {
      throw new TypeNotPresentException(type1, e);
    }
    Class<?> class2;
    try {
      class2 = Class.forName(type2.replace('/', '.'), false, classLoader);
    } catch (ClassNotFoundException e) {
      throw new TypeNotPresentException(type2, e);
    }
    if (class1.isAssignableFrom(class2)) {
      return type1;
    }
    if (class2.isAssignableFrom(class1)) {
      return type2;
    }
    if (class1.isInterface() || class2.isInterface()) {
      return "java/lang/Object";
    } else {
      do {
        class1 = class1.getSuperclass();
      } while (!class1.isAssignableFrom(class2));
      return class1.getName().replace('.', '/');
    }
  }
  
  // TODO: relview toByteArray() for annotations, inner-, outer-class

  static int javaClassVersion() {
    var v = System.getProperty("java.class.version").split("\\.");
    return Integer.parseInt(v[0]) | (Integer.parseInt(v[1]) << 16);
  }
  
  public byte[] toByteArray() {
    var size = 24 + 2 * interfaceCount;
    var fieldsCount = 0;
    var fieldWriter = firstField;
    while (fieldWriter != null) {
      ++fieldsCount;
      size += fieldWriter.computeFieldInfoSize();
      fieldWriter = (FieldWriter) fieldWriter.fv;
    }
    var methodsCount = 0;
    var methodWriter = firstMethod;
    while (methodWriter != null) {
      ++methodsCount;
      size += methodWriter.computeMethodInfoSize();
      methodWriter = (MethodWriter) methodWriter.mv;
    }
    var attributesCount = 0;
    if (innerClasses != null) {
      ++attributesCount;
      size += 8 + innerClasses.length;
      symbolTable.addConstantUtf8(Constants.INNER_CLASSES);
    }
    if (enclosingClassIndex != 0) {
      ++attributesCount;
      size += 10;
      symbolTable.addConstantUtf8(Constants.ENCLOSING_METHOD);
    }
    if (signatureIndex != 0) {
      ++attributesCount;
      size += 8;
      symbolTable.addConstantUtf8(Constants.SIGNATURE);
    }
    if (sourceFileIndex != 0) {
      ++attributesCount;
      size += 8;
      symbolTable.addConstantUtf8(Constants.SOURCE_FILE);
    }
    if (lastRuntimeVisibleAnnotation != null) {
      ++attributesCount;
      size += lastRuntimeVisibleAnnotation.computeAnnotationsSize(Constants.RUNTIME_VISIBLE_ANNOTATIONS);
    }
    if (lastRuntimeInvisibleAnnotation != null) {
      ++attributesCount;
      size += lastRuntimeInvisibleAnnotation.computeAnnotationsSize(Constants.RUNTIME_INVISIBLE_ANNOTATIONS);
    }
    if (symbolTable.computeBootstrapMethodsSize() > 0) {
      ++attributesCount;
      size += symbolTable.computeBootstrapMethodsSize();
    }
    size += symbolTable.getConstantPoolLength();
    var constantPoolCount = symbolTable.getConstantPoolCount();
    if (constantPoolCount > 0xFFFF) {
      throw new IndexOutOfBoundsException("Class too large: " + symbolTable.getClassName());
    }
    var result = new ByteVector(size);
    result.putInt(0xCAFEBABE).putInt(version);
    symbolTable.putConstantPool(result);
    var mask = 0; // version >= 1.5
    result.putShort(accessFlags & ~mask).putShort(thisClass).putShort(superClass);
    result.putShort(interfaceCount);
    for (var i = 0; i < interfaceCount; ++i) {
      result.putShort(interfaces[i]);
    }
    result.putShort(fieldsCount);
    fieldWriter = firstField;
    while (fieldWriter != null) {
      fieldWriter.putFieldInfo(result);
      fieldWriter = (FieldWriter) fieldWriter.fv;
    }
    result.putShort(methodsCount);
    methodWriter = firstMethod;
    while (methodWriter != null) {
      methodWriter.putMethodInfo(result);
      methodWriter = (MethodWriter) methodWriter.mv;
    }
    result.putShort(attributesCount);
    if (signatureIndex != 0) {
      result.putShort(symbolTable.addConstantUtf8(Constants.SIGNATURE)).putInt(2).putShort(signatureIndex);
    }
    if (sourceFileIndex != 0) {
      result.putShort(symbolTable.addConstantUtf8(Constants.SOURCE_FILE)).putInt(2).putShort(sourceFileIndex);
    }
    symbolTable.putBootstrapMethods(result);
    return result.data;
  }  
}
