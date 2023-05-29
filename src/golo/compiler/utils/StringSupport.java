package golo.compiler.utils;

public interface StringSupport {

  static String unescape(String str) {
    var sb = new StringBuilder(str.length());
    for (var i = 0; i < str.length(); i++) {
      var ch = str.charAt(i);
      if (ch == '\\') {
        char nextChar = (i == str.length() - 1) ? '\\' : str.charAt(i + 1);
        ch = switch (nextChar) {
          case 'u' -> (char) Integer.parseInt(str.substring(i + 2, (i += 4) + 2), 16);
          case '\\' -> '\\';
          case 'b' -> '\b';
          case 'f' -> '\f';
          case 'n' -> '\n';
          case 'r' -> '\r';
          case 't' -> '\t';
          case '\"' -> '\"';
          case '\'' -> '\'';
          default -> ch; // not a special char, do nothing
        };
        i++;
      }
      sb.append(ch);
    }
    return sb.toString();
  }
  
  static String unindent(String result, int i) {
    // TODO Auto-generated method stub
    return null;
  }

}
