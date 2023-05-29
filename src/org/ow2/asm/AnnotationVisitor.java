package org.ow2.asm;
public class AnnotationVisitor {

  AnnotationVisitor av;

  AnnotationVisitor(AnnotationVisitor annotationVisitor) {
    this.av = annotationVisitor;
  }

  public void visit(String name, Object value) {
    if (av != null) av.visit(name, value);
  }
  public void visitEnum(String name, String descriptor, String value) {
    if (av != null) av.visitEnum(name, descriptor, value);
  }
  public AnnotationVisitor visitArray(String name) {
    return av != null ? av.visitArray(name) : null;
  }
  public void visitEnd() {
    if (av != null) av.visitEnd();
  }
}
