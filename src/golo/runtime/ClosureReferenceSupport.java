package golo.runtime;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import golo.lang.FunctionReference;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodType.genericMethodType;

public final class ClosureReferenceSupport {

  private ClosureReferenceSupport() {
    throw new UnsupportedOperationException("Don't instantiate invokedynamic bootstrap class");
  }

  public static CallSite bootstrap(Lookup caller, String name, MethodType type, String moduleClass, int arity,
      int varargs) throws Throwable {
    Class<?> module = caller.lookupClass().getClassLoader().loadClass(moduleClass);
    Method function = module.getDeclaredMethod(name, genericMethodType(arity, varargs == 1).parameterArray());
    function.setAccessible(true);
    return new ConstantCallSite(
        constant(FunctionReference.class, new FunctionReference(caller.unreflect(function), parameterNames(function))));
  }

  private static String[] parameterNames(Method function) {
    Parameter[] parameters = function.getParameters();
    String[] parameterNames = new String[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      parameterNames[i] = parameters[i].getName();
    }
    return parameterNames;
  }

}
