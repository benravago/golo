package golo.runtime.spi;

import golo.compiler.GoloCompilationException;
import golo.lang.Messages;
import golo.lang.ir.GoloModule;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.lang.invoke.MethodHandle;
import java.io.File;

import static golo.lang.Messages.*;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;


public interface CliCommand {

  Comparator<GoloModule> MODULE_COMPARATOR = (GoloModule m1, GoloModule m2) -> {
    if (m1 == null && m2 != null) { return -1; }
    if (m1 != null && m2 == null) { return 1; }
    if (m1 == null && m2 == null) { return 0; }
    if (m1.hasMacros() && !m2.hasMacros()) { return -1; }
    if (!m1.hasMacros() && m2.hasMacros()) { return 1; }
    Set<String> m1Used = m1.getUsedModules();
    Set<String> m2Used = m2.getUsedModules();
    if (m1Used.contains(m2.getPackageAndClass().toString())) { return 1; }
    if (m2Used.contains(m1.getPackageAndClass().toString())) { return -1; }
    if (m1.getImports().stream().anyMatch((mi) -> mi.getPackageAndClass().equals(m2.getPackageAndClass()))) { return 1; }
    if (m2.getImports().stream().anyMatch((mi) -> mi.getPackageAndClass().equals(m1.getPackageAndClass()))) { return -1; }
    if (m1.hasMain() && !m2.hasMain()) { return 1; }
    if (m2.hasMain() && !m1.hasMain()) { return -1; }
    return 0;
  };

  void execute() throws Throwable;

  default void callRun(Class<?> klass, String[] arguments) throws Throwable {
    MethodHandle main;
    try {
      main = publicLookup().findStatic(klass, "main", methodType(void.class, String[].class));
    } catch (NoSuchMethodException e) {
      throw new NoMainMethodException().initCause(e);
    }
    main.invoke(arguments);
  }

  default boolean canRead(File source) {
    if (source == null) { return false; }
    if (!source.canRead()) {
      warning(message("file_not_found", source.getPath()));
      return false;
    }
    return true;
  }

  default Consumer<File> wrappedAction(boolean exitOnError, GolofileAction action) {
    return source -> {
      if (canRead(source)) {
        try {
          action.accept(source);
        } catch (GoloCompilationException e) {
          handleCompilationException(e, exitOnError);
        } catch (Throwable e) {
          handleThrowable(e, exitOnError);
        }
      }
    };
  }

  default Consumer<File> wrappedAction(GolofileAction action) {
    return wrappedAction(false, action);
  }

  default <T, R> Function<T, R> wrappedTreatment(GoloCompilationTreatment<T, R> t) {
    return data -> {
      if (data == null) {
        return null;
      }
      try {
        return t.apply(data);
      } catch (GoloCompilationException e) {
        handleCompilationException(e, false);
        return null;
      } catch (Throwable e) {
        handleThrowable(e, false);
        return null;
      }
    };
  }

  default <T> Function<T, T> displayInfo(String message) {
    if (this.verbose()) {
      return object -> {
        if (object != null) {
          info(String.format(message, object));
        }
        return object;
      };
    }
    return Function.identity();
  }

  default boolean verbose() {
    return false;
  }

  default void handleCompilationException(GoloCompilationException e) {
    handleCompilationException(e, true);
  }

  default void handleCompilationException(GoloCompilationException e, boolean exit) {
    Messages.error(e.getLocalizedMessage());
    for (GoloCompilationException.Problem problem : e.getProblems()) {
      Messages.error(problem.getDescription(), "  ");
      Throwable cause = problem.getCause();
      if (cause != null) {
        handleThrowable(cause, false, golo.lang.Runtime.debugMode(), "    ");
      }
    }
    if (exit) {
      System.exit(1);
    }
  }

  default void handleThrowable(Throwable e) {
    handleThrowable(e, golo.lang.Runtime.showStackTrace());
  }

  default void handleThrowable(Throwable e, boolean exit) {
    handleThrowable(e, exit, golo.lang.Runtime.debugMode() || golo.lang.Runtime.showStackTrace());
  }

  default void handleThrowable(Throwable e, boolean exit, boolean withStack) {
    handleThrowable(e, exit, withStack, "");
  }

  default void handleThrowable(Throwable e, boolean exit, boolean withStack, String indent) {
    Messages.error(e.getLocalizedMessage(), indent);
    if (e.getCause() != null) {
      Messages.error(e.getCause().getLocalizedMessage(), "  " + indent);
    }
    if (withStack) {
      Messages.printStackTrace(e);
    } else {
      Messages.error(Messages.message("use_debug"));
    }
    if (exit) {
      System.exit(1);
    }
  }

  class NoMainMethodException extends NoSuchMethodException { }

  @FunctionalInterface
  interface GolofileAction {
    void accept(File source) throws Throwable;
  }

  @FunctionalInterface
  interface GoloCompilationTreatment<T, R> {
    R apply(T o) throws Throwable;
  }

}
