package golo.compiler.utils;

public final class StringBlockIndenter {

  private StringBlockIndenter() {
    // utility class
  }

  public static String unindent(String block, int columns) {
    assert columns >= 0;
    String[] lines = block.split("\\n");
    StringBuilder result = new StringBuilder();
    for (String line : lines) {
      if (line.length() > columns) {
        result.append(line.substring(columns));
      } else {
        result.append(line);
      }
      result.append("\n");
    }
    return result.toString();
  }
}
