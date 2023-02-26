package org.objectweb.asm;
public class ClassWriter {
  
  public static final int COMPUTE_MAXS = 1;
  public static final int COMPUTE_FRAMES = 2;
  
  public ClassWriter(int flags) {}
  
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) { return null; }
  public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) { return null; }
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) { return null; }

  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {}
  public void visitEnd() {}
  public void visitInnerClass(String name, String outerName, String innerName, int access) {}
  public void visitOuterClass(String owner, String name, String descriptor) {}
  public void visitSource(String file, String debug) {}

  public byte[] toByteArray() { return null; }
}
