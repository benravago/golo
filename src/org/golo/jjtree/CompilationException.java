package org.golo.jjtree;

import golo.parser.ParseException;
import golo.parser.Node;

class CompilationException extends RuntimeException {
  CompilationException(String msg) { super(msg); }
  
  static class Builder {

    void report(ParseException e, Node node) {
      System.out.println("CompilationException -> "+e+' '+node);
    }
    
  }
}
