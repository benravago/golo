package golo.lang.ir;

import java.util.HashMap;

public enum OperatorType {

  PLUS("+"), MINUS("-"), TIMES("*"), DIVIDE("/"), MODULO("%"),

  EQUALS("=="), NOTEQUALS("!="), LESS("<"), LESSOREQUALS("<="), MORE(">"), MOREOREQUALS(">="),

  AND("and"), OR("or"), NOT("not"),

  IS("is"), ISNT("isnt"),

  OFTYPE("oftype"),

  ORIFNULL("orIfNull"),

  ANON_CALL(""), METHOD_CALL(":"), ELVIS_METHOD_CALL("?:");

  private final String symbol;

  private static final HashMap<String, OperatorType> SYMBOL_MAPPING = new HashMap<>();
  static {
    for (OperatorType op : values()) {
      SYMBOL_MAPPING.put(op.toString(), op);
    }
  }

  OperatorType(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }

  public static OperatorType of(Object value) {
    if (value instanceof OperatorType) {
      return (OperatorType) value;
    }
    if (SYMBOL_MAPPING.containsKey(value)) {
      return SYMBOL_MAPPING.get(value);
    }
    throw new IllegalArgumentException("An operator can't be create from " + value);
  }
}
