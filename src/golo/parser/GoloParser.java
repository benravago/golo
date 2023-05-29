package golo.parser;

import java.io.Reader;

import org.golo.jjtree.TreeParser;

public class GoloParser extends TreeParser {

  public GoloParser(Reader stream) {
    super(stream);
  }
  
  public static GoloParser offsetParser(Reader stream) {
    return new GoloParser(stream);
  }
}
