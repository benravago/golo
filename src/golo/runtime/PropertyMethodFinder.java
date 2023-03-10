package golo.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Objects;

import golo.lang.Union;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

class PropertyMethodFinder extends MethodFinder {

  private static final MethodHandle FLUENT_SETTER;

  static {
    try {
      FLUENT_SETTER = lookup().findStatic(PropertyMethodFinder.class, "fluentSetter",
          methodType(Object.class, Object.class, Object.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required fluent method handles", e);
    }
  }

  private static Object fluentSetter(Object o, Object notUsedSetterReturnValue) {
    return o;
  }

  private final String propertyName;

  PropertyMethodFinder(MethodInvocation invocation, Lookup lookup) {
    super(invocation, lookup);
    this.propertyName = capitalize(invocation.name());
  }

  private MethodHandle findMethodForGetter() {
    if (Union.class.isAssignableFrom(invocation.receiverClass())) {
      return null;
    }
    MethodHandle target = new RegularMethodFinder(invocation.withName("get" + propertyName), lookup).find();

    if (target != null) {
      return target;
    }
    return new RegularMethodFinder(invocation.withName("is" + propertyName), lookup).find();
  }

  private MethodHandle fluentMethodHandle(Method candidate) {
    Objects.requireNonNull(candidate);
    MethodHandle target = toMethodHandle(candidate).orElse(null);
    if (target != null) {
      if (!TypeMatching.returnsValue(candidate)) {
        Object receiver = invocation.arguments()[0];
        MethodHandle fluent = FLUENT_SETTER.bindTo(receiver);
        target = filterReturnValue(target, fluent);
      }
    }
    return target;
  }

  private MethodHandle findMethodForSetter() {
    return new RegularMethodFinder(invocation.withName("set" + propertyName), lookup).findInMethods()
        .filter(method -> !Union.class.isAssignableFrom(method.getDeclaringClass())).map(this::fluentMethodHandle)
        .findFirst().orElse(null);
  }

  @Override
  public MethodHandle find() {
    if (invocation.arity() == 1) {
      return findMethodForGetter();
    }
    return findMethodForSetter();
  }

  private static String capitalize(String word) {
    return Character.toUpperCase(word.charAt(0)) + word.substring(1);
  }
}
