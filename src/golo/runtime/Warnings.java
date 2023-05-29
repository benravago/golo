package golo.runtime;

import java.util.Arrays;
import java.util.HashSet;

import golo.lang.Tuple;

import static golo.Metadata.GUIDE_BASE;
import static golo.lang.Messages.message;
import static golo.lang.Messages.warning;

/**
 * A static class to deal with several kinds of warnings.
 */
public final class Warnings {
  private Warnings() {
    // utility class
  }

  private static final boolean NO_PARAMETER_NAMES = load("golo.warnings.no-parameter-names", "true");
  private static final boolean UNAVAILABLE_CLASS = load("golo.warnings.unavailable-class", "false");
  private static final boolean DEPRECATED = load("golo.warnings.deprecated", "true");
  private static final boolean MULTIPLE_PACKAGE_DESCRIPTION = load("golo.warnings.doc.multiple-package-desc", "true");
  private static final HashSet<Tuple> SEEN_DEPRECATIONS = new HashSet<>();

  private static boolean load(String property, String def) {
    return Boolean.valueOf(System.getProperty(property, def));
  }

  public static void multiplePackageDescription(String packageName) {
    if (MULTIPLE_PACKAGE_DESCRIPTION) {
      warning(message("multiple_package_desc", packageName, GUIDE_BASE));
    }
  }

  public static void noParameterNames(String methodName, String[] argumentNames) {
    if (NO_PARAMETER_NAMES || golo.lang.Runtime.debugMode()) {
      warning(message("no_parameter_names", methodName, Arrays.toString(argumentNames), GUIDE_BASE));
    }
  }

  public static void unavailableClass(String className, String callerModule) {
    if ((UNAVAILABLE_CLASS || golo.lang.Runtime.debugMode()) && !className.startsWith("java.lang")
        && !className.startsWith("gololang")) {
      warning(message("unavailable_class", className, callerModule, GUIDE_BASE));
    }
  }

  public static void deprecatedElement(String object, String caller) {
    if (DEPRECATED) {
      Tuple seen = new Tuple(object, caller);
      if (!SEEN_DEPRECATIONS.contains(seen)) {
        SEEN_DEPRECATIONS.add(seen);
        warning(message("deprecated_element", object, caller, GUIDE_BASE));
      }
    }
  }
}
