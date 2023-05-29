package org.ow2.asm;

class AnnotationWriter extends AnnotationVisitor {

  // TODO:
  
  static AnnotationWriter create(SymbolTable symbolTable, String descriptor, AnnotationWriter lastRuntimeVisibleAnnotation) {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

  AnnotationWriter(AnnotationVisitor annotationVisitor) {
    super(annotationVisitor);
  }
  
  int computeAnnotationsSize(String s) { return -1; }
}
