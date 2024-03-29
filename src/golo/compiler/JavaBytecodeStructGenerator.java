package golo.compiler;

import org.ow2.asm.ClassWriter;
import org.ow2.asm.FieldVisitor;
import org.ow2.asm.Label;
import org.ow2.asm.MethodVisitor;

import golo.lang.ir.Member;
import golo.lang.ir.Struct;

import static golo.compiler.JavaBytecodeUtils.addAnnotations;
import static golo.compiler.JavaBytecodeUtils.deprecatedFlag;
import static golo.compiler.JavaBytecodeUtils.loadInteger;
import static golo.lang.ir.Struct.IMMUTABLE_FACTORY_METHOD;
import static org.ow2.asm.ClassWriter.COMPUTE_FRAMES;
import static org.ow2.asm.ClassWriter.COMPUTE_MAXS;
import static org.ow2.asm.Opcodes.*;

class JavaBytecodeStructGenerator {

  private static final String $_frozen = "$_frozen";

  public CodeGenerationResult compile(Struct struct, String sourceFilename) {
    ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visitSource(sourceFilename, null);
    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER | ACC_FINAL | deprecatedFlag(struct),
        struct.getPackageAndClass().toJVMType(), null, "gololang/GoloStruct", null);
    addAnnotations(struct, classWriter::visitAnnotation);
    makeFields(classWriter, struct);
    makeAccessors(classWriter, struct);
    makeConstructors(classWriter, struct);
    makeImmutableFactory(classWriter, struct);
    makeToString(classWriter, struct);
    makeCopy(classWriter, struct, false);
    makeCopy(classWriter, struct, true);
    makeHashCode(classWriter, struct);
    makeEquals(classWriter, struct);
    makeToArrayMethod(classWriter, struct);
    makeGetMethod(classWriter, struct);
    makeSetMethod(classWriter, struct);
    classWriter.visitEnd();
    return new CodeGenerationResult(classWriter.toByteArray(), struct.getPackageAndClass(), sourceFilename);
  }

  private void makeSetMethod(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "set",
        "(Ljava/lang/String;Ljava/lang/Object;)L" + owner + ";", null, null);
    visitor.visitCode();
    insertPrivateElementCheck(struct, visitor);
    Label nextCase = new Label();
    for (Member member : struct.getMembers()) {
      visitor.visitLdcInsn(member.getName());
      visitor.visitVarInsn(ALOAD, 1);
      visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
      visitor.visitJumpInsn(IFEQ, nextCase);
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitVarInsn(ALOAD, 2);
      visitor.visitMethodInsn(INVOKEVIRTUAL, owner, member.getName(), "(Ljava/lang/Object;)L" + owner + ";", false);
      visitor.visitInsn(ARETURN);
      visitor.visitLabel(nextCase);
      nextCase = new Label();
    }
    insertUnknowElementCode(struct, visitor);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeGetMethod(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "get", "(Ljava/lang/String;)Ljava/lang/Object;", null,
        null);
    visitor.visitCode();
    insertPrivateElementCheck(struct, visitor);
    Label nextCase = new Label();
    for (Member member : struct.getMembers()) {
      visitor.visitLdcInsn(member.getName());
      visitor.visitVarInsn(ALOAD, 1);
      visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
      visitor.visitJumpInsn(IFEQ, nextCase);
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitMethodInsn(INVOKEVIRTUAL, owner, member.getName(), "()Ljava/lang/Object;", false);
      visitor.visitInsn(ARETURN);
      visitor.visitLabel(nextCase);
      nextCase = new Label();
    }
    insertUnknowElementCode(struct, visitor);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void throwLocalized(MethodVisitor visitor, String exceptionType, String message, String structName) {
    visitor.visitTypeInsn(NEW, exceptionType);
    visitor.visitInsn(DUP);
    visitor.visitLdcInsn(message);
    visitor.visitInsn(ICONST_1);
    visitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    visitor.visitInsn(DUP);
    visitor.visitInsn(ICONST_0);
    visitor.visitLdcInsn(structName);
    visitor.visitInsn(AASTORE);
    visitor.visitMethodInsn(INVOKESTATIC, "gololang/Messages", "message",
        "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false);
    visitor.visitMethodInsn(INVOKESPECIAL, exceptionType, "<init>", "(Ljava/lang/String;)V", false);
    visitor.visitInsn(ATHROW);
  }

  private void insertPrivateElementCheck(Struct struct, MethodVisitor visitor) {
    Label afterPrivateCheck = new Label();
    visitor.visitVarInsn(ALOAD, 1);
    visitor.visitLdcInsn("_");
    visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
    visitor.visitJumpInsn(IFEQ, afterPrivateCheck);
    throwLocalized(visitor, "java/lang/IllegalArgumentException", "struct_private_member",
        struct.getPackageAndClass().toString());
    visitor.visitLabel(afterPrivateCheck);
  }

  private void insertUnknowElementCode(Struct struct, MethodVisitor visitor) {
    throwLocalized(visitor, "java/lang/IllegalArgumentException", "unknown_struct_member",
        struct.getPackageAndClass().toString());
  }

  private void makeToArrayMethod(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "toArray", "()[Ljava/lang/Object;", null, null);
    visitor.visitCode();
    loadInteger(visitor, struct.getPublicMembers().size());
    visitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    int index = 0;
    for (Member member : struct.getPublicMembers()) {
      visitor.visitInsn(DUP);
      loadInteger(visitor, index);
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitFieldInsn(GETFIELD, owner, member.getName(), "Ljava/lang/Object;");
      visitor.visitInsn(AASTORE);
      index++;
    }
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeEquals(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
    Label notFrozenLabel = new Label();
    Label falseLabel = new Label();
    Label sameTypeLabel = new Label();
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitFieldInsn(GETFIELD, owner, $_frozen, "Z");
    visitor.visitJumpInsn(IFNE, notFrozenLabel);
    // super.equals()
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitVarInsn(ALOAD, 1);
    visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
    visitor.visitInsn(IRETURN);
    // The receiver is frozen
    visitor.visitLabel(notFrozenLabel);
    visitor.visitVarInsn(ALOAD, 1);
    visitor.visitTypeInsn(INSTANCEOF, owner);
    visitor.visitJumpInsn(IFNE, sameTypeLabel);
    visitor.visitJumpInsn(GOTO, falseLabel);
    // The argument is of the same type, too
    visitor.visitLabel(sameTypeLabel);
    visitor.visitVarInsn(ALOAD, 1);
    visitor.visitTypeInsn(CHECKCAST, owner);
    visitor.visitFieldInsn(GETFIELD, owner, $_frozen, "Z");
    visitor.visitJumpInsn(IFEQ, falseLabel);
    // The argument is not frozen
    for (Member member : struct.getMembers()) {
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitFieldInsn(GETFIELD, owner, member.getName(), "Ljava/lang/Object;");
      visitor.visitVarInsn(ALOAD, 1);
      visitor.visitTypeInsn(CHECKCAST, owner);
      visitor.visitFieldInsn(GETFIELD, owner, member.getName(), "Ljava/lang/Object;");
      visitor.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z",
          false);
      visitor.visitJumpInsn(IFEQ, falseLabel);
    }
    visitor.visitInsn(ICONST_1);
    visitor.visitInsn(IRETURN);
    // False
    visitor.visitLabel(falseLabel);
    visitor.visitInsn(ICONST_0);
    visitor.visitInsn(IRETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeHashCode(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
    Label notFrozenLabel = new Label();
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitFieldInsn(GETFIELD, owner, $_frozen, "Z");
    visitor.visitJumpInsn(IFNE, notFrozenLabel);
    // super.hashCode()
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "hashCode", "()I", false);
    visitor.visitInsn(IRETURN);
    // The receiver is frozen
    visitor.visitLabel(notFrozenLabel);
    loadInteger(visitor, struct.getMembers().size());
    visitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    int i = 0;
    for (Member member : struct.getMembers()) {
      visitor.visitInsn(DUP);
      loadInteger(visitor, i);
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitFieldInsn(GETFIELD, owner, member.getName(), "Ljava/lang/Object;");
      visitor.visitInsn(AASTORE);
      i++;
    }
    visitor.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "hash", "([Ljava/lang/Object;)I", false);
    visitor.visitInsn(IRETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeCopy(ClassWriter classWriter, Struct struct, boolean frozen) {
    String owner = struct.getPackageAndClass().toJVMType();
    String methodName = frozen ? "frozenCopy" : "copy";
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, methodName, "()L" + owner + ";", null, null);
    visitor.visitCode();
    visitor.visitTypeInsn(NEW, owner);
    visitor.visitInsn(DUP);
    for (Member member : struct.getMembers()) {
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitFieldInsn(GETFIELD, owner, member.getName(), "Ljava/lang/Object;");
    }
    visitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", allArgsConstructorSignature(struct), false);
    visitor.visitInsn(DUP);
    visitor.visitInsn(frozen ? ICONST_1 : ICONST_0);
    visitor.visitFieldInsn(PUTFIELD, owner, $_frozen, "Z");
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeToString(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
    visitor.visitCode();
    visitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
    visitor.visitInsn(DUP);
    visitor.visitLdcInsn("struct " + struct.getPackageAndClass().className() + "{");
    visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
    boolean first = true;
    for (Member member : struct.getPublicMembers()) {
      visitor.visitInsn(DUP);
      visitor.visitLdcInsn((!first ? ", " : "") + member.getName() + "=");
      first = false;
      visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
          "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      visitor.visitInsn(DUP);
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitFieldInsn(GETFIELD, owner, member.getName(), "Ljava/lang/Object;");
      visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
          "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
    }
    visitor.visitLdcInsn("}");
    visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
        "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
    visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeConstructors(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    makeNoArgsConstructor(classWriter, struct);
    makeAllArgsConstructor(classWriter, struct, owner);
  }

  private void makeAllArgsConstructor(ClassWriter classWriter, Struct struct, String owner) {
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC | deprecatedFlag(struct), "<init>",
        allArgsConstructorSignature(struct), null, null);
    addAnnotations(struct, visitor::visitAnnotation);
    for (Member member : struct.getMembers()) {
      visitor.visitParameter(member.getName(), ACC_FINAL);
    }
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitMethodInsn(INVOKESPECIAL, "gololang/GoloStruct", "<init>", "()V", false);
    int arg = 1;
    for (Member member : struct.getMembers()) {
      visitor.visitVarInsn(ALOAD, 0);
      visitor.visitVarInsn(ALOAD, arg);
      visitor.visitFieldInsn(PUTFIELD, owner, member.getName(), "Ljava/lang/Object;");
      arg++;
    }
    initMembersField(struct, owner, visitor);
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitInsn(ICONST_0);
    visitor.visitFieldInsn(PUTFIELD, owner, $_frozen, "Z");
    visitor.visitInsn(RETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeImmutableFactory(ClassWriter classWriter, Struct struct) {
    String constructorDesc = allArgsConstructorSignature(struct);
    String desc = constructorDesc.substring(0, constructorDesc.length() - 1);
    String classType = struct.getPackageAndClass().toJVMType();
    desc = desc + "L" + classType + ";";
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC | deprecatedFlag(struct),
        IMMUTABLE_FACTORY_METHOD, desc, null, null);
    for (Member member : struct.getMembers()) {
      visitor.visitParameter(member.getName(), ACC_FINAL);
    }
    visitor.visitCode();
    visitor.visitTypeInsn(NEW, classType);
    visitor.visitInsn(DUP);
    for (int i = 0; i < struct.getMembers().size(); i++) {
      visitor.visitVarInsn(ALOAD, i);
    }
    visitor.visitMethodInsn(INVOKESPECIAL, classType, "<init>", constructorDesc, false);
    visitor.visitInsn(DUP);
    visitor.visitInsn(ICONST_1);
    visitor.visitFieldInsn(PUTFIELD, classType, $_frozen, "Z");
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void initMembersField(Struct struct, String owner, MethodVisitor visitor) {
    int arg;
    visitor.visitVarInsn(ALOAD, 0);
    loadInteger(visitor, struct.getPublicMembers().size());
    visitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
    arg = 0;
    for (Member member : struct.getPublicMembers()) {
      visitor.visitInsn(DUP);
      loadInteger(visitor, arg);
      visitor.visitLdcInsn(member.getName());
      visitor.visitInsn(AASTORE);
      arg++;
    }
    visitor.visitFieldInsn(PUTFIELD, owner, "members", "[Ljava/lang/String;");
  }

  private String allArgsConstructorSignature(Struct struct) {
    StringBuilder signatureBuilder = new StringBuilder("(");
    for (int i = 0; i < struct.getMembers().size(); i++) {
      signatureBuilder.append("Ljava/lang/Object;");
    }
    signatureBuilder.append(")V");
    return signatureBuilder.toString();
  }

  private void makeNoArgsConstructor(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC | deprecatedFlag(struct), "<init>", "()V", null, null);
    addAnnotations(struct, visitor::visitAnnotation);
    for (Member member : struct.getMembers()) {
      visitor.visitParameter(member.getName(), ACC_FINAL);
    }
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitMethodInsn(INVOKESPECIAL, "gololang/GoloStruct", "<init>", "()V", false);
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitInsn(ICONST_0);
    visitor.visitFieldInsn(PUTFIELD, struct.getPackageAndClass().toJVMType(), $_frozen, "Z");
    initMembersField(struct, owner, visitor);
    visitor.visitInsn(RETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeFields(ClassWriter classWriter, Struct struct) {
    classWriter.visitField(ACC_PRIVATE | ACC_FINAL, $_frozen, "Z", null, null).visitEnd();
    for (Member member : struct.getMembers()) {
      FieldVisitor fieldVisitor = classWriter.visitField(ACC_PRIVATE, member.getName(), "Ljava/lang/Object;", null,
          null);
      fieldVisitor.visitEnd();
    }
  }

  private void makeAccessors(ClassWriter classWriter, Struct struct) {
    String owner = struct.getPackageAndClass().toJVMType();
    for (Member member : struct.getMembers()) {
      makeGetter(classWriter, owner, member.getName());
      makeSetter(classWriter, owner, member.getName(), struct);
    }
    makeFrozenGetter(classWriter, owner);
  }

  private void makeFrozenGetter(ClassWriter classWriter, String owner) {
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "isFrozen", "()Z", null, null);
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitFieldInsn(GETFIELD, owner, $_frozen, "Z");
    visitor.visitInsn(IRETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeSetter(ClassWriter classWriter, String owner, String name, Struct struct) {
    int accessFlag = name.startsWith("_") ? ACC_PRIVATE : ACC_PUBLIC;
    MethodVisitor visitor = classWriter.visitMethod(accessFlag, name,
        "(Ljava/lang/Object;)L" + struct.getPackageAndClass().toJVMType() + ";", null, null);
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitFieldInsn(GETFIELD, owner, $_frozen, "Z");
    Label setLabel = new Label();
    visitor.visitJumpInsn(IFEQ, setLabel);
    throwLocalized(visitor, "java/lang/IllegalStateException", "frozen_struct", struct.getPackageAndClass().toString());
    visitor.visitLabel(setLabel);
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitVarInsn(ALOAD, 1);
    visitor.visitFieldInsn(PUTFIELD, owner, name, "Ljava/lang/Object;");
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeGetter(ClassWriter classWriter, String owner, String name) {
    int accessFlag = name.startsWith("_") ? ACC_PRIVATE : ACC_PUBLIC;
    MethodVisitor visitor = classWriter.visitMethod(accessFlag, name, "()Ljava/lang/Object;", null, null);
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitFieldInsn(GETFIELD, owner, name, "Ljava/lang/Object;");
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }
}
