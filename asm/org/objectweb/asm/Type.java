package org.objectweb.asm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Type {

  public static final Type VOID_TYPE = null;

  public static String getConstructorDescriptor(Constructor<?> constructor) { return null; }
  public static String getDescriptor(Class<?> type) { return null; }
  public static String getInternalName(Class<?> type) { return null; }
  public static String getMethodDescriptor(Method method) { return null; }
  public static String getMethodDescriptor(Type returnType, Type... argumentTypes) { return null; }
  public static Type   getReturnType(Method method) { return null; }
  public static Type   getType(Class<?> type) { return null; }
}
