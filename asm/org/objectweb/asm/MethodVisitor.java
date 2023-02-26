package org.objectweb.asm;
public class MethodVisitor {

  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) { return null; }

  public void visitCode() {}
  public void visitEnd() {}
  public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {}
  public void visitInsn(int opcode) {}
  public void visitIntInsn(int opcode, int operand) {}
  public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {}
  public void visitJumpInsn(int opcode, Label label) {}
  public void visitLabel(Label label) {}
  public void visitLdcInsn(Object value) {}
  public void visitLineNumber(int line, Label start) {}
  public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {}
  public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {}
  public void visitMaxs(int maxStack, int maxLocals) {}
  public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {}
  public void visitParameter(String name, int access) {}
  public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {}
  public void visitTypeInsn(int opcode, String type) {}
  public void visitVarInsn(int opcode, int var) {}
}
