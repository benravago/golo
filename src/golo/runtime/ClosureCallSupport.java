package golo.runtime;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Arrays;

import golo.lang.FunctionReference;

import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodType.methodType;

public final class ClosureCallSupport {

  private ClosureCallSupport() {
    throw new UnsupportedOperationException("Don't instantiate invokedynamic bootstrap class");
  }

  static class InlineCache extends MutableCallSite {

    MethodHandle fallback;
    final boolean constant;
    final String[] argumentNames;

    InlineCache(MethodType type, boolean constant, String[] argumentNames) {
      super(type);
      this.constant = constant;
      this.argumentNames = argumentNames;
    }
  }

  private static final MethodHandle GUARD;
  private static final MethodHandle FALLBACK;

  static {
    try {
      Lookup lookup = MethodHandles.lookup();

      GUARD = lookup.findStatic(ClosureCallSupport.class, "guard",
          methodType(boolean.class, FunctionReference.class, FunctionReference.class));

      FALLBACK = lookup.findStatic(ClosureCallSupport.class, "fallback",
          methodType(Object.class, InlineCache.class, Object[].class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  public static CallSite bootstrap(Lookup caller, String name, MethodType type, Object... bsmArgs) {
    boolean constant = ((int) bsmArgs[0]) == 1;
    String[] argumentNames = new String[bsmArgs.length - 1];
    for (int i = 0; i < bsmArgs.length - 1; i++) {
      argumentNames[i] = (String) bsmArgs[i + 1];
    }
    InlineCache callSite = new InlineCache(type, constant, argumentNames);
    MethodHandle fallbackHandle = FALLBACK.bindTo(callSite).asCollector(Object[].class, type.parameterCount())
        .asType(type);
    callSite.fallback = fallbackHandle;
    callSite.setTarget(fallbackHandle);
    return callSite;
  }

  public static boolean guard(FunctionReference expected, FunctionReference actual) {
    return expected == actual;
  }

  public static Object fallback(InlineCache callSite, Object[] args) throws Throwable {
    FunctionReference targetFunctionReference = (FunctionReference) args[0];
    MethodHandle target = targetFunctionReference.handle();
    MethodHandle invoker = MethodHandles.dropArguments(target, 0, FunctionReference.class);
    MethodType type = invoker.type();
    if (callSite.argumentNames.length > 0) {
      invoker = reorderArguments(targetFunctionReference.parameterNames(), invoker, callSite.argumentNames);
    }
    if (target.isVarargsCollector()) {
      if (TypeMatching.isLastArgumentAnArray(type.parameterCount(), args)) {
        invoker = invoker.asFixedArity().asType(callSite.type());
      } else {
        invoker = invoker.asCollector(Object[].class, callSite.type().parameterCount() - target.type().parameterCount())
            .asType(callSite.type());
      }
    } else {
      invoker = invoker.asType(callSite.type());
    }
    if (callSite.constant) {
      Object constantValue = invoker.invokeWithArguments(args);
      MethodHandle constant;
      if (constantValue == null) {
        constant = MethodHandles.constant(Object.class, null);
      } else {
        constant = MethodHandles.constant(constantValue.getClass(), constantValue);
      }
      constant = MethodHandles.dropArguments(constant, 0, type.parameterArray());
      callSite.setTarget(constant.asType(type));
      return constantValue;
    } else {
      MethodHandle guard = GUARD.bindTo(targetFunctionReference);
      MethodHandle root = guardWithTest(guard, invoker, callSite.fallback);
      callSite.setTarget(root);
      return invoker.invokeWithArguments(args);
    }
  }

  private static MethodHandle reorderArguments(String[] parameterNames, MethodHandle handle, String[] argumentNames) {
    return NamedArgumentsHelper.reorderArguments("closure " + Arrays.toString(parameterNames),
        Arrays.asList(parameterNames), handle, argumentNames, 1, 1);
  }
}
