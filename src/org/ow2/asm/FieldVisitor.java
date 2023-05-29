package org.ow2.asm;
public class FieldVisitor {

  FieldVisitor fv;

  FieldVisitor(FieldVisitor fieldVisitor) {
    this.fv = fieldVisitor;
  }

  public void visitEnd() {
    if (fv != null) fv.visitEnd();
  }
}
