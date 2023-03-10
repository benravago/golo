package golo.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import golo.lang.GoloStruct;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.reflect.Modifier.*;

class RegularMethodFinder extends MethodFinder {

  private final boolean makeAccessible;

  RegularMethodFinder(MethodInvocation invocation, Lookup lookup) {
    super(invocation, lookup);
    this.makeAccessible = !isPublic(invocation.receiverClass().getModifiers());
  }

  @Override
  public MethodHandle find() {
    return Stream
        .concat(findInMethods().map(this::checkDeprecation).map(this::toMethodHandle),
            findInFields().map(this::checkDeprecation).map(this::toMethodHandle))
        .filter(Optional::isPresent).map(Optional::get).findFirst().orElse(null);
  }

  public boolean isOverloaded() {
    return Extractors.getMethods(invocation.receiverClass()).filter(this::overloadMatch).count() > 1;
  }

  private boolean overloadMatch(Method m) {
    return Extractors.isPublic(m) && Extractors.isConcrete(m) && m.getName().equals(invocation.name())
        && (m.getParameterCount() + 1 == invocation.arity())
        || (m.isVarArgs() && (m.getParameterCount() <= invocation.arity()));
  }

  private Optional<MethodHandle> toMethodHandle(Field field) {
    if (makeAccessible) {
      field.setAccessible(true);
    }
    try {
      if (invocation.arity() == 1) {
        return Optional.of(lookup.unreflectGetter(field).asType(invocation.type()));
      } else {
        return Optional.of(filterReturnValue(lookup.unreflectSetter(field),
            constant(invocation.receiverClass(), invocation.arguments()[0])).asType(invocation.type()));
      }
    } catch (IllegalAccessException e) {
      /* We need to give augmentations a chance, as IllegalAccessException can be noise in our resolution.
       * Example: augmenting HashSet with a map function.
       *  java.lang.IllegalAccessException: member is private: java.util.HashSet.map/java.util.HashMap/putField
       */
      return Optional.empty();
    }
  }

  @Override
  protected Optional<MethodHandle> toMethodHandle(Method method) {
    if (makeAccessible || isValidPrivateStructAccess(method)) {
      method.setAccessible(true);
    }
    return super.toMethodHandle(method)
        .map(handle -> FunctionCallSupport.insertSAMFilter(handle, lookup, method.getParameterTypes(), 1));
  }

  private boolean isValidPrivateStructAccess(Method method) {
    Object receiver = invocation.arguments()[0];
    if (!(receiver instanceof GoloStruct)) {
      return false;
    }
    String receiverClassName = receiver.getClass().getName();
    String callerClassName = callerClass.getName();
    return method.getName().equals(invocation.name()) && isPrivate(method.getModifiers())
        && (receiverClassName.startsWith(callerClassName)
            || callerClassName.equals(reverseStructAugmentation(receiverClassName)))
        && TypeMatching.argumentsMatch(method, invocation.arguments());
  }

  private static String reverseStructAugmentation(String receiverClassName) {
    return receiverClassName.substring(0, receiverClassName.indexOf(".types")) + "$"
        + receiverClassName.replace('.', '$');
  }

  protected Stream<Method> findInMethods() {
    return Extractors.getMethods(invocation.receiverClass())
        .filter(m -> invocation.match(m) || isValidPrivateStructAccess(m));
  }

  private Stream<Field> findInFields() {
    if (invocation.arity() > 3) {
      return Stream.empty();
    }
    return Extractors.getFields(invocation.receiverClass()).filter(this::isMatchingField);
  }

  private boolean isMatchingField(Field field) {
    return field.getName().equals(invocation.name()) && !isStatic(field.getModifiers());
  }

  private Field checkDeprecation(Field field) {
    return Extractors.checkDeprecation(callerClass, field);
  }

  private Method checkDeprecation(Method method) {
    return Extractors.checkDeprecation(callerClass, method);
  }

}
