package org.ow2.asm;
public class MethodVisitor {

  MethodVisitor mv;

  MethodVisitor(MethodVisitor methodVisitor) {
    this.mv = methodVisitor;
  }

  public void visitParameter(final String name, final int access) {
    if (mv != null) mv.visitParameter(name, access);
  }
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    return (mv != null) ? mv.visitAnnotation(descriptor, visible) : null;
  }
  public void visitCode() {
    if (mv != null) mv.visitCode();
  }
  public void visitInsn(int opcode) {
    if (mv != null) mv.visitInsn(opcode);
  }
  public void visitIntInsn(int opcode, int operand) {
    if (mv != null) mv.visitIntInsn(opcode, operand);
  }
  public void visitVarInsn(int opcode, int var) {
    if (mv != null) mv.visitVarInsn(opcode, var);
  }
  public void visitTypeInsn(int opcode, String type) {
    if (mv != null) mv.visitTypeInsn(opcode, type);
  }
  public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
    if (mv != null) mv.visitFieldInsn(opcode, owner, name, descriptor);
  }
  public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
    if (mv != null) mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
  }
  public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
    if (mv != null) mv.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
  }
  public void visitJumpInsn(int opcode, Label label) {
    if (mv != null) mv.visitJumpInsn(opcode, label);
  }
  public void visitLabel(Label label) {
    if (mv != null) mv.visitLabel(label);
  }
  public void visitLdcInsn(Object value) {
    if (mv != null) mv.visitLdcInsn(value);
  }
  void visitIincInsn(int var, int increment) {
    if (mv != null) mv.visitIincInsn(var, increment);
  }
  public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    if (mv != null) mv.visitLookupSwitchInsn(dflt, keys, labels);
  }
  public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    if (mv != null) mv.visitTryCatchBlock(start, end, handler, type);
  }
  public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
    if (mv != null) mv.visitLocalVariable(name, descriptor, signature, start, end, index);
  }
  public void visitLineNumber(int line, Label start) {
    if (mv != null) mv.visitLineNumber(line, start);
  }
  public void visitMaxs(int maxStack, int maxLocals) {
    if (mv != null) mv.visitMaxs(maxStack, maxLocals);
  }
  public void visitEnd() {
    if (mv != null) mv.visitEnd();
  }
}
