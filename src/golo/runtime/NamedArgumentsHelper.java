package golo.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import static java.util.stream.Collectors.toList;
import static golo.lang.Messages.message;
import static java.lang.invoke.MethodHandles.permuteArguments;

public final class NamedArgumentsHelper {

  private NamedArgumentsHelper() {
    // utility class
  }

  public static Boolean hasNamedParameters(Method method) {
    return Arrays.stream(method.getParameters()).allMatch(Parameter::isNamePresent);
  }

  public static List<String> getParameterNames(Method method) {
    if (hasNamedParameters(method)) {
      return Arrays.stream(method.getParameters()).map(Parameter::getName).collect(toList());
    }
    return Collections.emptyList();
  }

  public static void checkArgumentPosition(int position, String argument, String declaration) {
    if (position == -1) {
      throw new IllegalArgumentException(message("invalid_argument_name", argument, declaration));
    }
  }

  public static int[] getArgumentsOrder(String methodName, List<String> parameterNames, String[] argumentNames,
      int nameOffset, int orderOffset) {
    int[] argumentsOrder = new int[parameterNames.size() + orderOffset];
    for (int i = 0; i <= orderOffset; i++) {
      argumentsOrder[i] = i;
    }
    for (int i = 0; i < argumentNames.length; i++) {
      int actualPosition = parameterNames.indexOf(argumentNames[i]);
      checkArgumentPosition(actualPosition, argumentNames[i], methodName + parameterNames);
      argumentsOrder[actualPosition + orderOffset] = i + nameOffset;
    }
    return argumentsOrder;
  }

  public static MethodHandle reorderArguments(String methodName, List<String> parameterNames, MethodHandle handle,
      String[] argumentNames, int nameOffset, int orderOffset) {
    if (argumentNames.length == 0) {
      return handle;
    }
    if (parameterNames.isEmpty()) {
      Warnings.noParameterNames(methodName, argumentNames);
      return handle;
    }
    return permuteArguments(handle, handle.type(),
        getArgumentsOrder(methodName, parameterNames, argumentNames, nameOffset, orderOffset));
  }

}
