package golo.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Module {

  private static final Class<?>[] EMPTY_TYPES = new Class<?>[] {};
  private static final Object[] EMPTY_ARGS = {};

  private Module() {
    throw new UnsupportedOperationException("Don't instantiate utility classes");
  }

  static String[] metadata(String name, Class<?> callerClass, Class<?>[] types, Object[] args) {
    String[] data;
    try {
      Method dataMethod = callerClass.getMethod("$" + name, types);
      data = (String[]) dataMethod.invoke(null, args);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      // This can only happen as part of the unit tests, because the lookup does not originate from
      // a Golo module class, hence it doesn't have a $<name>() static method.
      data = new String[] {};
    }
    return data;
  }

  public static String[] imports(Class<?> callerClass) {
    return metadata("imports", callerClass, EMPTY_TYPES, EMPTY_ARGS);
  }

  public static String[] augmentations(Class<?> callerClass) {
    return metadata("augmentations", callerClass, EMPTY_TYPES, EMPTY_ARGS);
  }

  public static String[] augmentationApplications(Class<?> callerClass) {
    return metadata("augmentationApplications", callerClass, EMPTY_TYPES, EMPTY_ARGS);
  }

  public static String[] augmentationApplications(Class<?> callerClass, Class<?> receiverClass) {
    return metadata("augmentationApplications", callerClass, new Class<?>[] { int.class },
        new Object[] { receiverClass.getName().hashCode() });
  }
}
