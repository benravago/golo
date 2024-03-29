package golo.compiler;

import org.ow2.asm.ClassWriter;
import org.ow2.asm.Label;
import org.ow2.asm.MethodVisitor;

import golo.lang.ir.Member;
import golo.lang.ir.Union;
import golo.lang.ir.UnionValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import static golo.compiler.JavaBytecodeUtils.addAnnotations;
import static golo.compiler.JavaBytecodeUtils.deprecatedFlag;
import static golo.compiler.JavaBytecodeUtils.loadInteger;
import static org.ow2.asm.ClassWriter.COMPUTE_FRAMES;
import static org.ow2.asm.ClassWriter.COMPUTE_MAXS;
import static org.ow2.asm.Opcodes.*;

class JavaBytecodeUnionGenerator {

  public Collection<CodeGenerationResult> compile(Union union, String sourceFilename) {
    LinkedList<CodeGenerationResult> results = new LinkedList<>();
    ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visitSource(sourceFilename, null);
    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER | ACC_ABSTRACT | deprecatedFlag(union),
        union.getPackageAndClass().toJVMType(), null, "gololang/Union", null);
    addAnnotations(union, classWriter::visitAnnotation);
    makeDefaultConstructor(classWriter, "gololang/Union");
    HashMap<String, PackageAndClass> staticFields = new HashMap<>();
    for (UnionValue value : union.getValues()) {
      makeMatchlikeTestMethod(classWriter, value, false);
      results.add(makeUnionValue(classWriter, sourceFilename, value));
      if (value.hasMembers()) {
        makeStaticFactory(classWriter, value);
      } else {
        staticFields.put(value.getName(), value.getPackageAndClass());
      }
    }
    initStaticFields(classWriter, union.getPackageAndClass(), staticFields);
    classWriter.visitEnd();
    results.addFirst(new CodeGenerationResult(classWriter.toByteArray(), union.getPackageAndClass(), sourceFilename));
    return results;
  }

  private void initStaticFields(ClassWriter cw, PackageAndClass unionType, Map<String, PackageAndClass> staticFields) {
    MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();
    for (Map.Entry<String, PackageAndClass> attr : staticFields.entrySet()) {
      mv.visitTypeInsn(NEW, attr.getValue().toJVMType());
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, attr.getValue().toJVMType(), "<init>", "()V", false);
      mv.visitFieldInsn(PUTSTATIC, unionType.toJVMType(), attr.getKey(), unionType.toJVMRef());
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void makeStaticFactory(ClassWriter cw, UnionValue value) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, value.getName(),
        argsSignature(value.getMembers().size()) + value.getUnion().getPackageAndClass().toJVMRef(), null, null);
    for (Member member : value.getMembers()) {
      mv.visitParameter(member.getName(), ACC_FINAL);
    }
    mv.visitCode();
    mv.visitTypeInsn(NEW, value.getPackageAndClass().toJVMType());
    mv.visitInsn(DUP);
    for (int i = 0; i < value.getMembers().size(); i++) {
      mv.visitVarInsn(ALOAD, i);
    }
    mv.visitMethodInsn(INVOKESPECIAL, value.getPackageAndClass().toJVMType(), "<init>",
        argsSignature(value.getMembers().size()) + "V", false);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void makeDefaultConstructor(ClassWriter classWriter, String superCls) {
    MethodVisitor visitor = classWriter.visitMethod(ACC_PROTECTED, "<init>", "()V", null, null);
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitMethodInsn(INVOKESPECIAL, superCls, "<init>", "()V", false);
    visitor.visitInsn(RETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private void makeMatchlikeTestMethod(ClassWriter classWriter, UnionValue value, boolean result) {
    String methName = "is" + value.getName();
    MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, methName, "()Z", null, null);
    mv.visitCode();
    mv.visitInsn(result ? ICONST_1 : ICONST_0);
    mv.visitInsn(IRETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    if (value.hasMembers()) {
      mv = classWriter.visitMethod(ACC_PUBLIC, methName, argsSignature(value.getMembers().size()) + "Z", null, null);
      for (Member member : value.getMembers()) {
        mv.visitParameter(member.getName(), ACC_FINAL);
      }
      mv.visitCode();
      if (!result) {
        mv.visitInsn(ICONST_0);
      } else {
        int i = 1;
        Label allEquals = new Label();
        Label notEqual = new Label();
        String target = value.getPackageAndClass().toJVMType();
        for (Member member : value.getMembers()) {
          mv.visitVarInsn(ALOAD, i);
          mv.visitVarInsn(ALOAD, 0);
          mv.visitFieldInsn(GETFIELD, target, member.getName(), "Ljava/lang/Object;");
          mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z",
              false);
          mv.visitJumpInsn(IFEQ, notEqual);
          i++;
        }
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, allEquals);
        mv.visitLabel(notEqual);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(allEquals);
      }
      mv.visitInsn(IRETURN);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }
  }

  private void makeToString(ClassWriter classWriter, UnionValue value) {
    MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
    visitor.visitCode();
    if (!value.hasMembers()) {
      visitor.visitLdcInsn("union " + value.getUnion().getPackageAndClass().className() + "." + value.getName());
    } else {
      visitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
      visitor.visitInsn(DUP);
      visitor.visitLdcInsn("union " + value.getUnion().getPackageAndClass().className() + "." + value.getName() + "{");
      visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
      visitor.visitVarInsn(ASTORE, 1);
      boolean first = true;
      for (Member member : value.getMembers()) {
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitLdcInsn((first ? "" : ", ") + member.getName() + "=");
        visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        visitor.visitInsn(POP);

        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, value.getPackageAndClass().toJVMType(), member.getName(),
            "Ljava/lang/Object;");
        visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
            "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
        visitor.visitInsn(POP);
        first = false;
      }

      visitor.visitVarInsn(ALOAD, 1);
      visitor.visitLdcInsn("}");
      visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
          "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      visitor.visitInsn(POP);
      visitor.visitVarInsn(ALOAD, 1);
      visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
    }
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(0, 0);
    visitor.visitEnd();
  }

  private CodeGenerationResult makeUnionValue(ClassWriter parentClassWriter, String sourceFilename, UnionValue value) {
    String unionType = value.getUnion().getPackageAndClass().toJVMType();
    String valueType = value.getPackageAndClass().toJVMType();
    ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    classWriter.visitSource(sourceFilename, null);
    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER | ACC_FINAL | deprecatedFlag(value), valueType, null, unionType,
        null);
    addAnnotations(value, classWriter::visitAnnotation);
    classWriter.visitInnerClass(valueType, unionType, value.getName(),
        ACC_PUBLIC | ACC_FINAL | ACC_STATIC | deprecatedFlag(value));
    parentClassWriter.visitInnerClass(valueType, unionType, value.getName(),
        ACC_PUBLIC | ACC_FINAL | ACC_STATIC | deprecatedFlag(value));
    for (Member member : value.getMembers()) {
      classWriter.visitField(ACC_PUBLIC | ACC_FINAL, member.getName(), "Ljava/lang/Object;", null, null).visitEnd();
    }
    if (value.hasMembers()) {
      makeValuedConstructor(classWriter, value);
      makeHashCode(classWriter, value);
      makeEquals(classWriter, value);
      makeToArray(classWriter, value);
    } else {
      makeDefaultConstructor(classWriter, unionType);
      parentClassWriter.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC | deprecatedFlag(value), value.getName(),
          value.getUnion().getPackageAndClass().toJVMRef(), null, null).visitEnd();
    }
    makeToString(classWriter, value);
    makeMatchlikeTestMethod(classWriter, value, true);
    classWriter.visitEnd();
    return new CodeGenerationResult(classWriter.toByteArray(), value.getPackageAndClass(), sourceFilename);
  }

  private void makeEquals(ClassWriter cw, UnionValue value) {
    String target = value.getPackageAndClass().toJVMType();
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
    Label notSameInstance = new Label();
    Label notNull = new Label();
    Label sameType = new Label();
    Label allAttrsEquals = new Label();
    Label attrNotEqual = new Label();
    mv.visitCode();

    // if (other == this) { return true; }
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitJumpInsn(IF_ACMPNE, notSameInstance);
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IRETURN);
    mv.visitLabel(notSameInstance);

    // if (other == null) { return false; }
    mv.visitVarInsn(ALOAD, 1);
    mv.visitJumpInsn(IFNONNULL, notNull);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(IRETURN);
    mv.visitLabel(notNull);

    // if (!(other instanceof <value>)) { return false; }
    mv.visitVarInsn(ALOAD, 1);
    mv.visitTypeInsn(INSTANCEOF, target);
    mv.visitJumpInsn(IFNE, sameType);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(IRETURN);
    mv.visitLabel(sameType);

    // cast other to <value>
    mv.visitVarInsn(ALOAD, 1);
    mv.visitTypeInsn(CHECKCAST, target);
    mv.visitVarInsn(ASTORE, 2);

    // java.util.Objects.equals(<member>, other.<member>)
    for (Member member : value.getMembers()) {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, target, member.getName(), "Ljava/lang/Object;");
      mv.visitVarInsn(ALOAD, 2);
      mv.visitFieldInsn(GETFIELD, target, member.getName(), "Ljava/lang/Object;");
      mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
      mv.visitJumpInsn(IFEQ, attrNotEqual);
    }
    mv.visitInsn(ICONST_1);
    mv.visitJumpInsn(GOTO, allAttrsEquals);
    mv.visitLabel(attrNotEqual);
    mv.visitInsn(ICONST_0);
    mv.visitLabel(allAttrsEquals);
    mv.visitInsn(IRETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void makeHashCode(ClassWriter cw, UnionValue value) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
    mv.visitCode();
    loadMembersArray(mv, value);
    mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "hash", "([Ljava/lang/Object;)I", false);
    mv.visitInsn(IRETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void loadMembersArray(MethodVisitor mv, UnionValue value) {
    loadInteger(mv, value.getMembers().size());
    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    int i = 0;
    for (Member member : value.getMembers()) {
      mv.visitInsn(DUP);
      loadInteger(mv, i);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, value.getPackageAndClass().toJVMType(), member.getName(), "Ljava/lang/Object;");
      mv.visitInsn(AASTORE);
      i++;
    }
  }

  private void makeToArray(ClassWriter cw, UnionValue value) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "toArray", "()[Ljava/lang/Object;", null, null);
    mv.visitCode();
    loadMembersArray(mv, value);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private String argsSignature(int membersCount) {
    StringBuilder signature = new StringBuilder("(");
    for (int i = 0; i < membersCount; i++) {
      signature.append("Ljava/lang/Object;");
    }
    signature.append(")");
    return signature.toString();
  }

  private void makeValuedConstructor(ClassWriter cw, UnionValue value) {
    MethodVisitor mv = cw.visitMethod(ACC_PROTECTED | deprecatedFlag(value), "<init>",
        argsSignature(value.getMembers().size()) + "V", null, null);
    addAnnotations(value, mv::visitAnnotation);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, value.getUnion().getPackageAndClass().toJVMType(), "<init>", "()V", false);
    int idx = 1;
    for (Member member : value.getMembers()) {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, idx++);
      mv.visitFieldInsn(PUTFIELD, value.getPackageAndClass().toJVMType(), member.getName(), "Ljava/lang/Object;");
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }
}
