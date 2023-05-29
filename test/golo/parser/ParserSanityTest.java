package golo.parser;

import java.nio.file.Files;
import java.nio.file.Path;

import lib.junit5.PathSource;
import org.junit.jupiter.params.ParameterizedTest;

import org.golo.jjtree.TreeParser;

import static org.junit.jupiter.api.Assertions.*;

public class ParserSanityTest {
  
  @ParameterizedTest(name = "{0}")
  @PathSource(value="./test/for-parsing-and-compilation", match="**.golo")
  void test(Path path) throws Exception {
    var source = Files.newBufferedReader(path);  
    var parser = new TreeParser(source);
    var compilationUnit = parser.CompilationUnit();
    assertNotNull(compilationUnit);
  }
//import golo.parser.jjtree.GoloParser;
}