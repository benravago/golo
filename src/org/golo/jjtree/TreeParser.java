package org.golo.jjtree;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.math.BigDecimal;
import java.math.BigInteger;
import static java.lang.Integer.MAX_VALUE;

import golo.parser.ParseError;
import golo.parser.ParseException;
import golo.parser.Node;
import golo.parser.Token;
import golo.parser.ast.*;
import golo.compiler.utils.StringSupport;
import golo.lang.ir.ClassReference;
import golo.lang.ir.FunctionRef;

public class TreeParser implements ParserConstants {

  ParserState jjtree = new ParserState();
  public Object exceptionBuilder; // CompilationException.Builder exceptionBuilder;

  boolean errorAlreadyReported = false;

  void skipTo(int kind, ParseException e, Node node) {
    if (exceptionBuilder == null) {
      throw e;
    }
    if (!errorAlreadyReported) {
      // exceptionBuilder.report(e, node); // TODO: replace with BiConsumer<Exception,Node>
      errorAlreadyReported = true;
    }
    if ("<EOF>".equals(e.tokenImage[0])) {
      return;
    }
    Token t;
    do {
      t = getNextToken();
    } while (t.kind != kind);
  }

  void error() {
    var astError = new ParseError();
    jjtree.openNodeScope(astError);
    astError.firstToken = getToken(1);
    try {
      throw generateParseException();
    } finally {
      jjtree.closeNodeScope(astError, true);
      astError.lastToken = getToken(0);
    }
  }

  void BlankLine() {
    switch ((ntk == -1) ? ntk_f() : ntk) {
      case NEWLINE -> consumeToken(NEWLINE);
      case COMMENT -> consumeToken(COMMENT);
      default -> { la1[0] = gen; consumeToken(-1); throw new ParseException(); }
    }
  }

  void BlankLines() {
    while (isBlankLine(2)) {
      BlankLine();
    }
  }

  String QualifiedName() {
    var rootToken = consumeToken(IDENTIFIER);
    var nameBuilder = new StringBuilder(rootToken.image);
    loop: for(;;) {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case Dot -> {}
        default -> { la1[1] = gen; break loop; }
      }
      consumeToken(Dot);
      var nextToken = consumeToken(IDENTIFIER);
      nameBuilder.append(".").append(nextToken.image);
    }
    return nameBuilder.toString();
  }

  // TODO: allows macros on function parameters ?
  List<String> Parameters() {
    var parameters = new ArrayList<String>();
    var rootToken = consumeToken(IDENTIFIER);
    parameters.add(rootToken.image);
    loop: for(;;) {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case Comma -> {}
        default -> { la1[2] = gen; break loop; }
      }
      consumeToken(Comma);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case NEWLINE, COMMENT -> BlankLine();
        default -> la1[3] = gen;
      }
      var nextToken = consumeToken(IDENTIFIER);
      parameters.add(nextToken.image);
    }
    return parameters;
  }

  List<String> AugmentationNames() {
    var names = new ArrayList<String>();
    var rootToken = QualifiedName();
    names.add(rootToken);
    loop: for(;;) {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case Comma -> {}
        default -> { la1[4] = gen; break loop; }
      }
      consumeToken(Comma);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case NEWLINE, COMMENT -> BlankLine();
        default -> la1[5] = gen;
      }
      var nextToken = QualifiedName();
      names.add(nextToken);
    }
    return names;
  }

  String StringLiteral() {
    switch ((ntk == -1) ? ntk_f() : ntk) {
      case STRING -> {
        var literal = consumeToken(STRING);
        return StringSupport.unescape(literal.image.substring(1, literal.image.length() - 1));
      }
      case MULTI_STRING -> {
        var literal = consumeToken(MULTI_STRING);
        return literal.image.substring(3, literal.image.length() - 3);
      }
      default -> {
        la1[6] = gen;
        consumeToken(-1);
        throw new ParseException();
      }
    }
  }

  Character CharLiteral() {
    var literal = consumeToken(CHAR);
    return StringSupport.unescape(literal.image.substring(1, literal.image.length() - 1)).charAt(0);
  }

  Long LongLiteral() {
    var literal = consumeToken(LONG_NUMBER);
    var image = literal.image.substring(0, literal.image.length() - 2);
    return Long.valueOf(image.replace("_", ""));
  }

  BigInteger BigIntegerLiteral() {
    var literal = consumeToken(BIGINTEGER);
    var image = literal.image.substring(0, literal.image.length() - 2);
    return new BigInteger(image.replace("_", ""));
  }

  Integer IntegerLiteral() {
    var literal = consumeToken(NUMBER);
    var image = literal.image.replace("_", "");
    return Integer.valueOf(image);
  }

  ClassReference ClassLiteral() {
    var literal = consumeToken(CLASSREF);
    var image = literal.image;
    var suffixLength = image.endsWith("class") ? 6 : 7;
    return ClassReference.of(image.substring(0, image.length() - suffixLength));
  }

  Double DoubleLiteral() {
    var literal = consumeToken(FLOATING_NUMBER);
    return Double.valueOf(literal.image.replace("_", ""));
  }

  Float FloatLiteral() {
    var literal = consumeToken(FLOAT);
    return Float.valueOf(literal.image.substring(0, literal.image.length() - 2).replace("_", ""));
  }

  BigDecimal BigDecimalLiteral() {
    var literal = consumeToken(BIGDECIMAL);
    return new BigDecimal(literal.image.substring(0, literal.image.length() - 2).replace("_", ""));
  }

  FunctionRef FunctionRef() {
    var literal = consumeToken(FUNREF);
    String module = null, name;
    var arity = -1;
    var varargs = false;
    var parts = literal.image.substring(1).split("::");
    if (parts.length > 1) {
      module = parts[0];
      name = parts[1];
    } else {
      name = parts[0];
    }
    parts = name.split("\\\\");
    if (parts.length > 1) {
      name = parts[0];
      if (parts[1].endsWith("...")) {
        arity = Integer.parseInt(parts[1].substring(0, parts[1].length() - 3));
        varargs = true;
      } else {
        arity = Integer.parseInt(parts[1]);
      }
    }
    return FunctionRef.of(module, name, arity, varargs);
  }

  String Documentation() {
    var token = consumeToken(DOCUMENTATION);
    var result = token.image.trim();
    result = result.substring(4, result.length() - 4);
    if (token.beginColumn > 1) {
      result = StringSupport.unindent(result, token.beginColumn - 1);
    }
    return result;
  }

  public CompilationUnit CompilationUnit() {
    var astCompilationUnit = new CompilationUnit();
    var open = jjtree.openNodeScope(astCompilationUnit);
    astCompilationUnit.firstToken = getToken(1);
    try {
      try {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, MODULE, DECORATOR, COMMENT, DOCUMENTATION -> {
            BlankLines();
            ModuleDeclaration();
            BlankLines();
            loop1: for(;;) {
              switch ((ntk == -1) ? ntk_f() : ntk) {
                case IMPORT -> {}
                default -> { la1[7] = gen; break loop1; }
              }
              ImportDeclaration();
              BlankLines();
            }
            loop2: for(;;) {
              switch ((ntk == -1) ? ntk_f() : ntk) {
                case FUNCTION, LOCAL, AUGMENT, NAMEDAUGMENTATION, STRUCT, UNION, DECORATOR, MACRO_INVOCATION, MACRO, VAR, LET, DOCUMENTATION -> {}
                default -> { la1[8] = gen; break loop2; }
              }
              try {
                switch ((ntk == -1) ? ntk_f() : ntk) {
                  case FUNCTION, LOCAL, AUGMENT, NAMEDAUGMENTATION, STRUCT, UNION, DECORATOR, MACRO_INVOCATION, MACRO, DOCUMENTATION -> {
                    ToplevelDeclaration();
                    BlankLines();
                  }
                  case VAR, LET -> {
                    var state = LetOrVar();
                    BlankLines();
                    state.isModule = true;
                  }
                  default -> {
                    la1[9] = gen;
                    consumeToken(-1);
                    throw new ParseException();
                  }
                }
              } catch (ParseException e) {
                skipTo(NEWLINE, e, astCompilationUnit);
              }
            }
          }
          default -> { la1[10] = gen; error(); }
        }
        consumeToken(0);
      } catch (ParseException e) {
        skipTo(NEWLINE, e, astCompilationUnit);
      }
      jjtree.closeNodeScope(astCompilationUnit, true);
      open = false;
      astCompilationUnit.lastToken = getToken(0);
      return astCompilationUnit;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astCompilationUnit);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astCompilationUnit, true);
        astCompilationUnit.lastToken = getToken(0);
      }
    }
  }

  void ModuleDeclaration() {
    var astModuleDeclaration = new ModuleDeclaration();
    var open = jjtree.openNodeScope(astModuleDeclaration);
    astModuleDeclaration.firstToken = getToken(1);
    String documentation = null;
    try {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case DOCUMENTATION -> documentation = Documentation();
        default -> la1[11] = gen;
      }
      loop:
      for(;;) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case DECORATOR -> {}
          default -> { la1[12] = gen; break loop; }
        }
        DecoratorDeclaration();
      }
      consumeToken(MODULE);
      var name = QualifiedName();
      jjtree.closeNodeScope(astModuleDeclaration, true);
      open = false;
      astModuleDeclaration.lastToken = getToken(0);
      astModuleDeclaration.name = name;
      astModuleDeclaration.documentation = documentation;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astModuleDeclaration);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astModuleDeclaration, true);
        astModuleDeclaration.lastToken = getToken(0);
      }
    }
  }

  List<String> MultiImport() {
    var modules = new ArrayList<String>();
    consumeToken(DotBrace);
    switch ((ntk == -1) ? ntk_f() : ntk) {
      case NEWLINE, COMMENT -> BlankLine();
      default -> la1[13] = gen;
    }
    var rootName = QualifiedName();
    modules.add(rootName);
    loop: for(;;) {
      consumeToken(Comma);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case NEWLINE, COMMENT -> BlankLine();
        default -> la1[14] = gen;
      }
      var nextName = QualifiedName();
      modules.add(nextName);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case Comma -> {}
        default -> { la1[15] = gen; break loop; }
      }
    }
    switch ((ntk == -1) ? ntk_f() : ntk) {
      case NEWLINE, COMMENT -> BlankLine();
      default -> la1[16] = gen;
    }
    consumeToken(RightBrace);
    return modules;
  }

  void ImportDeclaration() {
    var astImportDeclaration = new ImportDeclaration();
    var open = jjtree.openNodeScope(astImportDeclaration);
    astImportDeclaration.firstToken = getToken(1);
    Token relative = null;
    List<String> multiples = null;
    try {
      try {
        consumeToken(IMPORT);
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case Dot -> relative = consumeToken(Dot);
          default -> la1[17] = gen;
        }
        var name = QualifiedName();
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case DotBrace -> multiples = MultiImport();
          default -> la1[18] = gen;
        }
        astImportDeclaration.name = name;
        if (relative != null) {
          astImportDeclaration.isRelative = true;
        }
        if (multiples != null) {
          astImportDeclaration.multiples = multiples;
        }
      } catch (ParseException e) {
        skipTo(NEWLINE, e, astImportDeclaration);
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astImportDeclaration);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astImportDeclaration, true);
        astImportDeclaration.lastToken = getToken(0);
      }
    }
  }

  void ToplevelDeclaration() {
    var astTopLevelDeclaration = new TopLevelDeclaration();
    var open = jjtree.openNodeScope(astTopLevelDeclaration);
    astTopLevelDeclaration.firstToken = getToken(1);
    Token local = null;
    String documentation = null;
    try {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case FUNCTION, LOCAL, AUGMENT, NAMEDAUGMENTATION, STRUCT, UNION, DECORATOR, MACRO, DOCUMENTATION -> {
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case DOCUMENTATION -> documentation = Documentation();
            default -> la1[19] = gen;
          }
          loop: for(;;) {
            switch ((ntk == -1) ? ntk_f() : ntk) {
              case DECORATOR -> {}
              default -> { la1[20] = gen; break loop; }
            }
            DecoratorDeclaration();
          }
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case FUNCTION, LOCAL, MACRO -> {
              switch ((ntk == -1) ? ntk_f() : ntk) {
                case LOCAL -> local = consumeToken(LOCAL);
                default -> la1[21] = gen;
              }
              var functionDecl = FunctionDeclaration();
              jjtree.closeNodeScope(astTopLevelDeclaration, true);
              open = false;
              astTopLevelDeclaration.lastToken = getToken(0);
              if (local != null) {
                functionDecl.isLocal = true;
              }
              functionDecl.documentation = documentation;
            }
            case STRUCT -> {
              var structDecl = StructDeclaration();
              jjtree.closeNodeScope(astTopLevelDeclaration, true);
              open = false;
              astTopLevelDeclaration.lastToken = getToken(0);
              structDecl.documentation = documentation;
            }
            case UNION -> {
              var unionDecl = UnionDeclaration();
              jjtree.closeNodeScope(astTopLevelDeclaration, true);
              open = false;
              astTopLevelDeclaration.lastToken = getToken(0);
              unionDecl.documentation = documentation;
            }
            case AUGMENT -> {
              var augmentDecl = AugmentDeclaration();
              jjtree.closeNodeScope(astTopLevelDeclaration, true);
              open = false;
              astTopLevelDeclaration.lastToken = getToken(0);
              augmentDecl.documentation = documentation;
            }
            case NAMEDAUGMENTATION -> {
              var namedAugmentationDecl = NamedAugmentationDeclaration();
              jjtree.closeNodeScope(astTopLevelDeclaration, true);
              open = false;
              astTopLevelDeclaration.lastToken = getToken(0);
              namedAugmentationDecl.documentation = documentation;
            }
            default -> {
              la1[22] = gen;
              consumeToken(-1);
              throw new ParseException();
            }
          }
        }
        case MACRO_INVOCATION -> {
          var macro = MacroInvocation();
          jjtree.closeNodeScope(astTopLevelDeclaration, true);
          open = false;
          astTopLevelDeclaration.lastToken = getToken(0);
          macro.isTopLevel = true;
        }
        default -> {
          la1[23] = gen;
          consumeToken(-1);
          throw new ParseException();
        }
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astTopLevelDeclaration);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astTopLevelDeclaration, true);
        astTopLevelDeclaration.lastToken = getToken(0);
      }
    }
  }

  MemberDeclaration MemberDeclaration() {
    var astMemberDeclaration = new MemberDeclaration();
    var open = jjtree.openNodeScope(astMemberDeclaration);
    astMemberDeclaration.firstToken = getToken(1);
    String documentation = null;
    try {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case DOCUMENTATION -> documentation = Documentation();
        default -> la1[24] = gen;
      }
      var name = consumeToken(IDENTIFIER);
      jjtree.closeNodeScope(astMemberDeclaration, true);
      open = false;
      astMemberDeclaration.lastToken = getToken(0);
      astMemberDeclaration.name = name.image;
      astMemberDeclaration.firstToken = name;
      astMemberDeclaration.documentation = documentation;
      return astMemberDeclaration;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astMemberDeclaration);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astMemberDeclaration, true);
        astMemberDeclaration.lastToken = getToken(0);
      }
    }
  }

  StructDeclaration StructDeclaration() {
    var astStructDeclaration = new StructDeclaration();
    var open = jjtree.openNodeScope(astStructDeclaration);
    astStructDeclaration.firstToken = getToken(1);
    try {
      consumeToken(STRUCT);
      var name = consumeToken(IDENTIFIER);
      consumeToken(Equals);
      consumeToken(LeftBrace);
      BlankLines();
      MemberDeclaration();
      BlankLines();
      loop: for(;;) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case Comma -> {}
          default -> { la1[25] = gen; break loop; }
        }
        consumeToken(Comma);
        BlankLines();
        MemberDeclaration();
        BlankLines();
      }
      consumeToken(RightBrace);
      jjtree.closeNodeScope(astStructDeclaration, true);
      open = false;
      astStructDeclaration.lastToken = getToken(0);
      astStructDeclaration.name = name.image;
      return astStructDeclaration;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astStructDeclaration);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astStructDeclaration, true);
        astStructDeclaration.lastToken = getToken(0);
      }
    }
  }

  void UnionValue() {
    var astUnionValue = new UnionValue();
    var open = jjtree.openNodeScope(astUnionValue);
    astUnionValue.firstToken = getToken(1);
    String documentation = null;
    try {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case DOCUMENTATION -> documentation = Documentation();
        default -> la1[26] = gen;
      }
      loop: for(;;) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case DECORATOR -> {}
          default -> { la1[27] = gen; break loop; }
        }
        DecoratorDeclaration();
      }
      var name = consumeToken(IDENTIFIER);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case Equals -> {
          consumeToken(Equals);
          consumeToken(LeftBrace);
          BlankLines();
          MemberDeclaration();
          BlankLines();
          loop: for(;;) {
            switch ((ntk == -1) ? ntk_f() : ntk) {
              case Comma -> {}
              default -> { la1[28] = gen; break loop; }
            }
            consumeToken(Comma);
            BlankLines();
            MemberDeclaration();
            BlankLines();
          }
          consumeToken(RightBrace);
        }
        default -> la1[29] = gen;
      }
      jjtree.closeNodeScope(astUnionValue, true);
      open = false;
      astUnionValue.lastToken = getToken(0);
      astUnionValue.name = name.image;
      astUnionValue.firstToken = name;
      astUnionValue.documentation = documentation;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astUnionValue);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astUnionValue, true);
        astUnionValue.lastToken = getToken(0);
      }
    }
  }

  UnionDeclaration UnionDeclaration() {
    var astUnionDeclaration = new UnionDeclaration();
    var open = jjtree.openNodeScope(astUnionDeclaration);
    astUnionDeclaration.firstToken = getToken(1);
    try {
      consumeToken(UNION);
      var name = consumeToken(IDENTIFIER);
      consumeToken(Equals);
      consumeToken(LeftBrace);
      BlankLines();
      loop: for(;;) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case DECORATOR, IDENTIFIER, DOCUMENTATION -> {}
          default -> { la1[30] = gen; break loop; }
        }
        UnionValue();
        BlankLines();
      }
      consumeToken(RightBrace);
      jjtree.closeNodeScope(astUnionDeclaration, true);
      open = false;
      astUnionDeclaration.lastToken = getToken(0);
      astUnionDeclaration.name = name.image;
      return astUnionDeclaration;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astUnionDeclaration);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astUnionDeclaration, true);
        astUnionDeclaration.lastToken = getToken(0);
      }
    }
  }

  void AugmentationDeclaration() {
    String documentation = null;
    consumeToken(LeftBrace);
    BlankLines();
    loop1: for(;;) {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case FUNCTION, DECORATOR, MACRO_INVOCATION, MACRO, DOCUMENTATION -> {}
        default -> { la1[31] = gen; break loop1; }
      }
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case FUNCTION, DECORATOR, MACRO, DOCUMENTATION -> {
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case DOCUMENTATION -> documentation = Documentation();
            default -> la1[32] = gen;
          }
          loop2: for(;;) {
            switch ((ntk == -1) ? ntk_f() : ntk) {
              case DECORATOR -> {}
              default -> { la1[33] = gen; break loop2; }
            }
            DecoratorDeclaration();
          }
          var func = FunctionDeclaration();
          func.isAugmentation = true;
          func.documentation = documentation;
        }
        case MACRO_INVOCATION -> {
          var macro = MacroInvocation();
          macro.isTopLevel = true;
        }
        default -> {
          la1[34] = gen;
          consumeToken(-1);
          throw new ParseException();
        }
      }
      BlankLines();
    }
    consumeToken(RightBrace);
  }

  NamedAugmentationDeclaration NamedAugmentationDeclaration() {
    var astNamedAugmentDeclaration = new NamedAugmentationDeclaration();
    var open = jjtree.openNodeScope(astNamedAugmentDeclaration);
    astNamedAugmentDeclaration.firstToken = getToken(1);
    try {
      consumeToken(NAMEDAUGMENTATION);
      var name = consumeToken(IDENTIFIER);
      consumeToken(Equals);
      AugmentationDeclaration();
      jjtree.closeNodeScope(astNamedAugmentDeclaration, true);
      open = false;
      astNamedAugmentDeclaration.lastToken = getToken(0);
      astNamedAugmentDeclaration.name = name.image;
      return astNamedAugmentDeclaration;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astNamedAugmentDeclaration);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astNamedAugmentDeclaration, true);
        astNamedAugmentDeclaration.lastToken = getToken(0);
      }
    }
  }

  AugmentDeclaration AugmentDeclaration() {
    var astAugmentDeclaration = new AugmentDeclaration();
    var open = jjtree.openNodeScope(astAugmentDeclaration);
    astAugmentDeclaration.firstToken = getToken(1);
    List<String> names = null;
    try {
      consumeToken(AUGMENT);
      var target = QualifiedName();
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case LeftBrace -> {
          AugmentationDeclaration();
        }
        case WITH -> {
          consumeToken(WITH);
          names = AugmentationNames();
        }
        default -> {
          la1[35] = gen;
          consumeToken(-1);
          throw new ParseException();
        }
      }
      jjtree.closeNodeScope(astAugmentDeclaration, true);
      open = false;
      astAugmentDeclaration.lastToken = getToken(0);
      astAugmentDeclaration.target = target;
      astAugmentDeclaration.augmentationNames = names;
      return astAugmentDeclaration;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astAugmentDeclaration);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astAugmentDeclaration, true);
        astAugmentDeclaration.lastToken = getToken(0);
      }
    }
  }

  FunctionDeclaration FunctionDeclaration() {
    var astFunctionDeclaration = new FunctionDeclaration();
    var open = jjtree.openNodeScope(astFunctionDeclaration);
    astFunctionDeclaration.firstToken = getToken(1);
    try {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case FUNCTION -> {
          consumeToken(FUNCTION);
        }
        case MACRO -> {
          consumeToken(MACRO);
          astFunctionDeclaration.isMacro = true;
        }
        default -> {
          la1[36] = gen;
          consumeToken(-1);
          throw new ParseException();
        }
      }
      var name = consumeToken(IDENTIFIER);
      consumeToken(Equals);
      Function();
      jjtree.closeNodeScope(astFunctionDeclaration, true);
      open = false;
      astFunctionDeclaration.lastToken = getToken(0);
      astFunctionDeclaration.name = name.image;
      return astFunctionDeclaration;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astFunctionDeclaration);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astFunctionDeclaration, true);
        astFunctionDeclaration.lastToken = getToken(0);
      }
    }
  }

  DecoratorDeclaration DecoratorDeclaration() {
    var astDecoratorDeclaration = new DecoratorDeclaration();
    var open = jjtree.openNodeScope(astDecoratorDeclaration);
    astDecoratorDeclaration.firstToken = getToken(1);
    Token constant = null;
    try {
      consumeToken(DECORATOR);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case CONSTANT_INVOCATION ->  constant = consumeToken(CONSTANT_INVOCATION);
        default -> la1[37] = gen;
      }
      if (isInvocationExpression(2)) {
        InvocationExpression();
      } else if (aDecoratorReference(2)) {
        Reference();
      } else {
        consumeToken(-1);
        throw new ParseException();
      }
      BlankLines();
      jjtree.closeNodeScope(astDecoratorDeclaration, true);
      open = false;
      astDecoratorDeclaration.lastToken = getToken(0);
      if (constant != null) {
        astDecoratorDeclaration.isConstant = true;
      }
      return astDecoratorDeclaration;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astDecoratorDeclaration);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astDecoratorDeclaration, true);
        astDecoratorDeclaration.lastToken = getToken(0);
      }
    }
  }

  void Statement() {
    switch ((ntk == -1) ? ntk_f() : ntk) {
      case NEWLINE, COMMENT -> {
        BlankLine();
      }
      default -> {
        la1[38] = gen;
        if (isAssignmentStatement(2)) {
          Assignment();
        } else if (isDestructuringAssignment(3)) {
          DestructuringAssignment();
        } else if (isLetOrVar(2)) {
          LetOrVar();
        } else {
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case MATCH,
                 UNARY_OPERATOR,
                 MACRO_INVOCATION,
                 NUMBER, LONG_NUMBER, BIGINTEGER, FLOATING_NUMBER, FLOAT, BIGDECIMAL, STRING, CHAR, NULL, TRUE, FALSE,
                 CLASSREF, FUNREF,
                 COLL_START,
                 MULTI_STRING,
                 IDENTIFIER,
                 LeftBrace, LeftParenthesis, Bar, Arrow -> ExpressionStatement();
            case RETURN -> Return();
            case IF -> ConditionalBranching();
            case WHILE -> WhileLoop();
            case FOR -> ForLoop();
            case FOREACH -> ForEachLoop();
            case THROW -> Throw();
            case TRY -> TryCatchFinally();
            case CASE -> Case();
            case CONTINUE -> Continue();
            case BREAK -> Break();
            default -> {
              la1[39] = gen;
              consumeToken(-1);
              throw new ParseException();
            }
          }
        }
      }
    }
  }

  void Continue() {
    var astContinue = new Continue();
    var open = jjtree.openNodeScope(astContinue);
    astContinue.firstToken = getToken(1);
    try {
      consumeToken(CONTINUE);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astContinue, true);
        astContinue.lastToken = getToken(0);
      }
    }
  }

  void Break() {
    var astBreak = new Break();
    var open = jjtree.openNodeScope(astBreak);
    astBreak.firstToken = getToken(1);
    try {
      consumeToken(BREAK);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astBreak, true);
        astBreak.lastToken = getToken(0);
      }
    }
  }

  void Throw() {
    var astThrow = new Throw();
    var open = jjtree.openNodeScope(astThrow);
    astThrow.firstToken = getToken(1);
    try {
      consumeToken(THROW);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case NEWLINE, COMMENT -> BlankLine();
        default -> la1[40] = gen;
      }
      ExpressionStatement();
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astThrow);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astThrow, true);
        astThrow.lastToken = getToken(0);
      }
    }
  }

  void WhileLoop() {
    var astWhileLoop = new WhileLoop();
    var open = jjtree.openNodeScope(astWhileLoop);
    astWhileLoop.firstToken = getToken(1);
    try {
      consumeToken(WHILE);
      ExpressionStatement();
      Block();
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astWhileLoop);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astWhileLoop, true);
        astWhileLoop.lastToken = getToken(0);
      }
    }
  }

  void ForLoop() {
    var astForLoop = new ForLoop();
    var open = jjtree.openNodeScope(astForLoop);
    astForLoop.firstToken = getToken(1);
    try {
      consumeToken(FOR);
      consumeToken(LeftParenthesis);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case NEWLINE, COMMENT -> BlankLine();
        default -> la1[41] = gen;
      }
      LetOrVar();
      consumeToken(Comma);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case NEWLINE, COMMENT -> BlankLine();
        default -> la1[42] = gen;
      }
      ExpressionStatement();
      consumeToken(Comma);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case NEWLINE, COMMENT -> BlankLine();
        default -> la1[43] = gen;
      }
      Statement();
      consumeToken(RightParenthesis);
      if (aForLoopBlock(2)) {
        Block();
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astForLoop);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astForLoop, true);
        astForLoop.lastToken = getToken(0);
      }
    }
  }

  void ForEachLoop() {
    var astForEachLoop = new ForEachLoop();
    var open = jjtree.openNodeScope(astForEachLoop);
    astForEachLoop.firstToken = getToken(1);
    Token elementId;
    List<String> names;
    Token varargsToken = null;
    try {
      consumeToken(FOREACH);
      if (aForEachLoop(2)) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case IDENTIFIER -> {
            elementId = consumeToken(IDENTIFIER);
            consumeToken(IN);
            ExpressionStatement();
            switch ((ntk == -1) ? ntk_f() : ntk) {
              case WHEN -> {
                consumeToken(WHEN);
                ExpressionStatement();
              }
              default -> la1[44] = gen;
            }
          }
          case LeftParenthesis -> {
            consumeToken(LeftParenthesis);
            elementId = consumeToken(IDENTIFIER);
            consumeToken(IN);
            ExpressionStatement();
            switch ((ntk == -1) ? ntk_f() : ntk) {
              case WHEN -> {
                consumeToken(WHEN);
                ExpressionStatement();
              }
              default -> la1[45] = gen;
            }
            consumeToken(RightParenthesis);
          }
          default -> {
            la1[46] = gen;
            consumeToken(-1);
            throw new ParseException();
          }
        }
        astForEachLoop.elementIdentifier = elementId.image;
      } else if (aForEachLoopDestructuredNames(2)) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case IDENTIFIER -> {
            names = DestructuredNames();
            switch ((ntk == -1) ? ntk_f() : ntk) {
              case Ellipsis -> varargsToken = consumeToken(Ellipsis);
              default -> la1[47] = gen;
            }
            consumeToken(IN);
            ExpressionStatement();
            switch ((ntk == -1) ? ntk_f() : ntk) {
              case WHEN -> {
                consumeToken(WHEN);
                ExpressionStatement();
              }
              default -> la1[48] = gen;
            }
          }
          case LeftParenthesis -> {
            consumeToken(LeftParenthesis);
            names = DestructuredNames();
            switch ((ntk == -1) ? ntk_f() : ntk) {
              case Ellipsis -> varargsToken = consumeToken(Ellipsis);
              default -> la1[49] = gen;
            }
            consumeToken(IN);
            ExpressionStatement();
            switch ((ntk == -1) ? ntk_f() : ntk) {
              case WHEN -> {
                consumeToken(WHEN);
                ExpressionStatement();
              }
              default -> la1[50] = gen;
            }
            consumeToken(RightParenthesis);
          }
          default -> {
            la1[51] = gen;
            consumeToken(-1);
            throw new ParseException();
          }
        }
        astForEachLoop.names = names;
        astForEachLoop.isVarargs = (varargsToken != null);
      } else {
        consumeToken(-1);
        throw new ParseException();
      }
      if (aForEaachLoopBlock(2)) {
        Block();
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astForEachLoop);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astForEachLoop, true);
        astForEachLoop.lastToken = getToken(0);
      }
    }
  }

  void TryCatchFinally() {
    var astTryCatchFinally = new TryCatchFinally();
    var open = jjtree.openNodeScope(astTryCatchFinally);
    astTryCatchFinally.firstToken = getToken(1);
    try {
      consumeToken(TRY);
      Block();
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case FINALLY -> {
          consumeToken(FINALLY);
          Block();
          jjtree.closeNodeScope(astTryCatchFinally, true);
          open = false;
          astTryCatchFinally.lastToken = getToken(0);
          astTryCatchFinally.exceptionId = null;
        }
        case CATCH -> {
          consumeToken(CATCH);
          consumeToken(LeftParenthesis);
          var exceptionId = consumeToken(IDENTIFIER);
          consumeToken(RightParenthesis);
          Block();
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case FINALLY -> {
              consumeToken(FINALLY);
              Block();
            }
            default -> la1[52] = gen;
          }
          jjtree.closeNodeScope(astTryCatchFinally, true);
          open = false;
          astTryCatchFinally.lastToken = getToken(0);
          astTryCatchFinally.exceptionId = exceptionId.image;
        }
        default -> {
          la1[53] = gen;
          consumeToken(-1);
          throw new ParseException();
        }
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astTryCatchFinally);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astTryCatchFinally, true);
        astTryCatchFinally.lastToken = getToken(0);
      }
    }
  }

  void ExpressionStatement() {
    var astExpressionStatement = new ExpressionStatement();
    var open = jjtree.openNodeScope(astExpressionStatement);
    astExpressionStatement.firstToken = getToken(1);
    try {
      OrIfNullExpression();
      if (isLocalDeclaration(2)) {
        LocalDeclaration();
      }
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case NEWLINE, COMMENT -> BlankLine();
        default -> la1[54] = gen;
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astExpressionStatement);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astExpressionStatement, true);
        astExpressionStatement.lastToken = getToken(0);
      }
    }
  }

  void LocalDeclaration() {
    var astLocalDeclaration = new LocalDeclaration();
    var open = jjtree.openNodeScope(astLocalDeclaration);
    astLocalDeclaration.firstToken = getToken(1);
    try {
      consumeToken(WITH);
      consumeToken(LeftBrace);
      loop: for(;;) {
        if (isLocalAssignment(3)) {
          Assignment();
        } else {
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case VAR, LET, IDENTIFIER -> {
              DestructuringAssignment();
            }
            case NEWLINE, COMMENT -> {
              BlankLine();
            }
            default -> {
              la1[55] = gen;
              consumeToken(-1);
              throw new ParseException();
            }
          }
        }
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, VAR, LET, IDENTIFIER, COMMENT -> {}
          default -> { la1[56] = gen; break loop; }
        }
      }
      consumeToken(RightBrace);
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astLocalDeclaration);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astLocalDeclaration, true);
        astLocalDeclaration.lastToken = getToken(0);
      }
    }
  }

  void Atom() {
    if (isFunctionInvocation(2)) {
      FunctionInvocation();
    } else if (anAtomReference(2)) {
      Reference();
    } else {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case MACRO_INVOCATION -> {
          MacroInvocation();
        }
        case MATCH -> {
          Match();
        }
        case NUMBER, LONG_NUMBER, BIGINTEGER, FLOATING_NUMBER, FLOAT, BIGDECIMAL, STRING, CHAR, NULL, TRUE, FALSE, CLASSREF, FUNREF, MULTI_STRING -> {
          Literal();
        }
        case LeftBrace, Bar, Arrow -> {
          Function();
        }
        case COLL_START -> {
          CollectionLiteral();
        }
        case LeftParenthesis -> {
          consumeToken(LeftParenthesis);
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case NEWLINE, COMMENT -> {
              BlankLine();
            }
            default -> {
              la1[57] = gen;
              ;
            }
          }
          ExpressionStatement();
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case NEWLINE, COMMENT -> {
              BlankLine();
            }
            default -> {
              la1[58] = gen;
              ;
            }
          }
          consumeToken(RightParenthesis);
        }
        default -> {
          la1[59] = gen;
          consumeToken(-1);
          throw new ParseException();
        }
      }
    }
  }

  void UnaryExpression() {
    Token token = null;
    switch ((ntk == -1) ? ntk_f() : ntk) {
      case UNARY_OPERATOR -> token = consumeToken(UNARY_OPERATOR);
      default -> la1[60] = gen;
    }
    InvocationExpression();
    var astUnaryExpression = new UnaryExpression();
    var open = jjtree.openNodeScope(astUnaryExpression);
    astUnaryExpression.firstToken = getToken(1);
    try {
      jjtree.closeNodeScope(astUnaryExpression, token != null);
      open = false;
      astUnaryExpression.lastToken = getToken(0);
      if (token != null) {
        astUnaryExpression.operator = token.image;
      }
    } finally {
      if (open) {
        jjtree.closeNodeScope(astUnaryExpression, token != null);
        astUnaryExpression.lastToken = getToken(0);
      }
    }
  }

  void InvocationExpression() {
    Token token = null;
    Atom();
    var astInvocationExpression = new InvocationExpression();
    var open = jjtree.openNodeScope(astInvocationExpression);
    astInvocationExpression.firstToken = getToken(1);
    try {
      while (anInvocationExpression(2)) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[61] = gen;
        }
        token = consumeToken(INVOCATION_OPERATOR);
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[62] = gen;
        }
        MethodInvocation();
        astInvocationExpression.operators.add(token.image);
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astInvocationExpression);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astInvocationExpression, token != null);
        astInvocationExpression.lastToken = getToken(0);
      }
    }
    while (isAnonymousInvocationExpression(2)) {
      var anon = AnonymousFunctionInvocation();
      anon.isOnExpression = true;
    }
  }

  void MultiplicativeExpression() {
    Token token = null;
    UnaryExpression();
    var astMultiplicativeExpression = new MultiplicativeExpression();
    var open = jjtree.openNodeScope(astMultiplicativeExpression);
    astMultiplicativeExpression.firstToken = getToken(1);
    try {
      while (aMultiplicativeExpression(2)) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[63] = gen;
        }
        token = consumeToken(MULTIPLICATIVE_OPERATOR);
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[64] = gen;
        }
        InvocationExpression();
        astMultiplicativeExpression.operators.add(token.image);
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astMultiplicativeExpression);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astMultiplicativeExpression, token != null);
        astMultiplicativeExpression.lastToken = getToken(0);
      }
    }
  }

  void AdditiveExpression() {
    Token token = null;
    MultiplicativeExpression();
    var astAdditiveExpression = new AdditiveExpression();
    var open = jjtree.openNodeScope(astAdditiveExpression);
    astAdditiveExpression.firstToken = getToken(1);
    try {
      while (anAdditiveExpression(2)) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[65] = gen;
        }
        token = consumeToken(ADDITIVE_OPERATOR);
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[66] = gen;
        }
        MultiplicativeExpression();
        astAdditiveExpression.operators.add(token.image);
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astAdditiveExpression);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astAdditiveExpression, token != null);
        astAdditiveExpression.lastToken = getToken(0);
      }
    }
  }

  void RelationalExpression() {
    Token token = null;
    AdditiveExpression();
    var astRelationalExpression = new RelationalExpression();
    var open = jjtree.openNodeScope(astRelationalExpression);
    astRelationalExpression.firstToken = getToken(1);
    try {
      if (aRelationalExpression(2)) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[67] = gen;
        }
        token = consumeToken(RELATIONAL_OPERATOR);
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[68] = gen;
        }
        AdditiveExpression();
        astRelationalExpression.operator = token.image;
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astRelationalExpression);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astRelationalExpression, token != null);
        astRelationalExpression.lastToken = getToken(0);
      }
    }
  }

  void EqualityExpression() {
    Token token = null;
    RelationalExpression();
    var astEqualityExpression = new EqualityExpression();
    var open = jjtree.openNodeScope(astEqualityExpression);
    astEqualityExpression.firstToken = getToken(1);
    try {
      if (anEqualityExpression(2)) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[69] = gen;
        }
        token = consumeToken(EQUALITY_OPERATOR);
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[70] = gen;
        }
        RelationalExpression();
        astEqualityExpression.operator = token.image;
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astEqualityExpression);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astEqualityExpression, token != null);
        astEqualityExpression.lastToken = getToken(0);
      }
    }
  }

  void AndExpression() {
    var count = 0;
    EqualityExpression();
    var astAndExpression = new AndExpression();
    var open = jjtree.openNodeScope(astAndExpression);
    astAndExpression.firstToken = getToken(1);
    try {
      while (anAndExpression(2)) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[71] = gen;
        }
        consumeToken(AND_OPERATOR);
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[72] = gen;
        }
        EqualityExpression();
        astAndExpression.count += 1;
        count++;
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astAndExpression);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astAndExpression, count > 0);
        astAndExpression.lastToken = getToken(0);
      }
    }
  }

  void OrExpression() {
    var count = 0;
    AndExpression();
    var astorExpression = new OrExpression();
    var open = jjtree.openNodeScope(astorExpression);
    astorExpression.firstToken = getToken(1);
    try {
      while (anOrExpression(2)) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[73] = gen;
        }
        consumeToken(OR_OPERATOR);
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[74] = gen;
        }
        AndExpression();
        astorExpression.count += 1;
        count++;
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astorExpression);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astorExpression, count > 0);
        astorExpression.lastToken = getToken(0);
      }
    }
  }

  void OrIfNullExpression() {
    var count = 0;
    OrExpression();
    var astOrIfNullExpression = new OrIfNullExpression();
    var open = jjtree.openNodeScope(astOrIfNullExpression);
    astOrIfNullExpression.firstToken = getToken(1);
    try {
      while (anOrIfNullExpression(2)) {
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[75] = gen;
        }
        consumeToken(OR_IFNULL_OPERATOR);
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case NEWLINE, COMMENT -> BlankLine();
          default -> la1[76] = gen;
        }
        OrExpression();
        astOrIfNullExpression.count += 1;
        count++;
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astOrIfNullExpression);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astOrIfNullExpression, count > 0);
        astOrIfNullExpression.lastToken = getToken(0);
      }
    }
  }

  void MethodInvocation() {
    var astMethodInvocation = new MethodInvocation();
    var open = jjtree.openNodeScope(astMethodInvocation);
    astMethodInvocation.firstToken = getToken(1);
    try {
      var token = consumeToken(IDENTIFIER);
      Arguments();
      while (isAnonymousMethodInvocation(2)) {
        AnonymousFunctionInvocation();
      }
      jjtree.closeNodeScope(astMethodInvocation, true);
      open = false;
      astMethodInvocation.lastToken = getToken(0);
      astMethodInvocation.name = token.image;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astMethodInvocation);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astMethodInvocation, true);
        astMethodInvocation.lastToken = getToken(0);
      }
    }
  }

  void Block() {
    var astBlock = new Block();
    var open = jjtree.openNodeScope(astBlock);
    astBlock.firstToken = getToken(1);
    try {
      try {
        consumeToken(LeftBrace);
        loop: for(;;) {
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case NEWLINE, RETURN, IF, WHILE, FOR, FOREACH, THROW, TRY, CASE, MATCH, BREAK, CONTINUE, UNARY_OPERATOR, MACRO_INVOCATION, VAR, LET, NUMBER, LONG_NUMBER, BIGINTEGER, FLOATING_NUMBER, FLOAT, BIGDECIMAL, STRING, CHAR, NULL, TRUE, FALSE, CLASSREF, FUNREF, COLL_START, MULTI_STRING, IDENTIFIER, COMMENT,
                 LeftBrace, LeftParenthesis, Bar, Arrow -> {}
            default -> { la1[77] = gen; break loop; }
          }
          try {
            Statement();
          } catch (ParseException e) {
            skipTo(NEWLINE, e, astBlock);
          }
        }
        consumeToken(RightBrace);
      } catch (ParseException e) {
        skipTo(NEWLINE, e, astBlock);
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astBlock);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astBlock, true);
        astBlock.lastToken = getToken(0);
      }
    }
  }

  MacroInvocation MacroInvocation() {
    var astMacroInvocation = new MacroInvocation();
    var open = jjtree.openNodeScope(astMacroInvocation);
    astMacroInvocation.firstToken = getToken(1);
    try {
      var token = consumeToken(MACRO_INVOCATION);
      var name = QualifiedName();
      if (aMacroInvocationArguments(2)) {
        Arguments();
      }
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case LeftBrace -> {
          if (TreeParser.this.aMacroInvocation(4)) {
            consumeToken(LeftBrace);
            BlankLines();
            loop: for(;;) {
              try {
                switch ((ntk == -1) ? ntk_f() : ntk) {
                  case IMPORT -> {
                    ImportDeclaration();
                    BlankLines();
                  }
                  case FUNCTION, LOCAL, AUGMENT, NAMEDAUGMENTATION, STRUCT, UNION, DECORATOR, MACRO_INVOCATION, MACRO, DOCUMENTATION -> {
                    ToplevelDeclaration();
                    BlankLines();
                  }
                  default -> {
                    la1[78] = gen;
                    consumeToken(-1);
                    throw new ParseException();
                  }
                }
              } catch (ParseException e) {
                skipTo(NEWLINE, e, astMacroInvocation);
              }
              switch ((ntk == -1) ? ntk_f() : ntk) {
                case IMPORT, FUNCTION, LOCAL, AUGMENT, NAMEDAUGMENTATION, STRUCT, UNION, DECORATOR, MACRO_INVOCATION, MACRO, DOCUMENTATION -> {}
                default -> { la1[79] = gen; break loop; }
              }
            }
            BlankLines();
            consumeToken(RightBrace);
          } else {
            switch ((ntk == -1) ? ntk_f() : ntk) {
              case LeftBrace -> {
                Block();
              }
              default -> {
                la1[80] = gen;
                consumeToken(-1);
                throw new ParseException();
              }
            }
          }
        }
        default -> la1[81] = gen;
      }
      jjtree.closeNodeScope(astMacroInvocation, true);
      open = false;
      astMacroInvocation.lastToken = getToken(0);
      astMacroInvocation.name = name;
      return astMacroInvocation;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astMacroInvocation);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astMacroInvocation, true);
        astMacroInvocation.lastToken = getToken(0);
      }
    }
  }

  void Function() {
    var astFunction = new Function();
    var open = jjtree.openNodeScope(astFunction);
    astFunction.firstToken = getToken(1);
    List<String> parameters = null;
    Token varArgsToken = null;
    var compactForm = false;
    try {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case Bar -> {
          consumeToken(Bar);
          parameters = Parameters();
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case Ellipsis -> varArgsToken = consumeToken(Ellipsis);
            default -> la1[82] = gen;
          }
          consumeToken(Bar);
        }
        default -> la1[83] = gen;
      }
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case LeftBrace -> {
          Block();
        }
        case Arrow -> {
          consumeToken(Arrow);
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case NEWLINE, COMMENT -> BlankLine();
            default -> la1[84] = gen;
          }
          ExpressionStatement();
          compactForm = true;
        }
        default -> {
          la1[85] = gen;
          consumeToken(-1);
          throw new ParseException();
        }
      }
      jjtree.closeNodeScope(astFunction, true);
      open = false;
      astFunction.lastToken = getToken(0);
      if (parameters == null) {
        parameters = Collections.emptyList();
      }
      astFunction.isCompactForm = compactForm;
      astFunction.parameters = parameters;
      astFunction.isVarargs = (varArgsToken != null);
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astFunction);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astFunction, true);
        astFunction.lastToken = getToken(0);
      }
    }
  }

  void Literal() {
    var astLiteral = new Literal();
    var open = jjtree.openNodeScope(astLiteral);
    astLiteral.firstToken = getToken(1);
    try {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case CLASSREF -> {
          var value = ClassLiteral();
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = value;
        }
        case STRING, MULTI_STRING -> {
          var value = StringLiteral();
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = value;
        }
        case CHAR -> {
          var value = CharLiteral();
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = value;
        }
        case BIGDECIMAL -> {
          var value = BigDecimalLiteral();
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = value;
        }
        case BIGINTEGER -> {
          var value = BigIntegerLiteral();
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = value;
        }
        case FLOAT -> {
          var value = FloatLiteral();
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = value;
        }
        case FLOATING_NUMBER -> {
          var value = DoubleLiteral();
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = value;
        }
        case LONG_NUMBER -> {
          var value = LongLiteral();
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = value;
        }
        case NUMBER -> {
          var value = IntegerLiteral();
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = value;
        }
        case NULL -> {
          consumeToken(NULL);
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = null;
        }
        case TRUE -> {
          consumeToken(TRUE);
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = Boolean.TRUE;
        }
        case FALSE -> {
          consumeToken(FALSE);
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = Boolean.FALSE;
        }
        case FUNREF -> {
          var value = FunctionRef();
          jjtree.closeNodeScope(astLiteral, true);
          open = false;
          astLiteral.lastToken = getToken(0);
          astLiteral.literalValue = value;
        }
        default -> {
          la1[86] = gen;
          consumeToken(-1);
          throw new ParseException();
        }
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astLiteral);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astLiteral, true);
        astLiteral.lastToken = getToken(0);
      }
    }
  }

  void CollectionLiteral() {
    var astCollectionLiteral = new CollectionLiteral();
    var open = jjtree.openNodeScope(astCollectionLiteral);
    astCollectionLiteral.firstToken = getToken(1);
    // Token token;
    var isRange = false;
    var isComprehension = false;
    try {
      var token = consumeToken(COLL_START);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case NEWLINE, COMMENT -> BlankLine();
        default -> la1[87] = gen;
      }
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case MATCH, UNARY_OPERATOR, MACRO_INVOCATION, NUMBER, LONG_NUMBER, BIGINTEGER, FLOATING_NUMBER, FLOAT, BIGDECIMAL, STRING, CHAR, NULL, TRUE, FALSE, CLASSREF, FUNREF, COLL_START, MULTI_STRING, IDENTIFIER, LeftBrace, LeftParenthesis, Bar, Arrow -> {
          ExpressionStatement();
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case FOR, FOREACH, Comma, DotDot -> {
              switch ((ntk == -1) ? ntk_f() : ntk) {
                case Comma -> {
                  loop: for(;;) {
                    consumeToken(Comma);
                    switch ((ntk == -1) ? ntk_f() : ntk) {
                      case NEWLINE, COMMENT -> BlankLine();
                      default -> la1[88] = gen;
                    }
                    ExpressionStatement();
                    switch ((ntk == -1) ? ntk_f() : ntk) {
                      case Comma -> {}
                      default -> { la1[89] = gen; break loop; }
                    }
                  }
                }
                case DotDot -> {
                  consumeToken(DotDot);
                  ExpressionStatement();
                  isRange = true;
                }
                case FOR, FOREACH -> {
                  loop: for(;;) {
                    switch ((ntk == -1) ? ntk_f() : ntk) {
                      case FOREACH -> {
                        ForEachLoop();
                      }
                      case FOR -> {
                        ForLoop();
                      }
                      default -> {
                        la1[90] = gen;
                        consumeToken(-1);
                        throw new ParseException();
                      }
                    }
                    switch ((ntk == -1) ? ntk_f() : ntk) {
                      case FOR, FOREACH -> {}
                      default -> { la1[91] = gen; break loop; }
                    }
                  }
                  isComprehension = true;
                }
                default -> {
                  la1[92] = gen;
                  consumeToken(-1);
                  throw new ParseException();
                }
              }
            }
            default -> la1[93] = gen;
          }
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case NEWLINE, COMMENT ->  BlankLine();
            default -> la1[94] = gen;
          }
        }
        default -> la1[95] = gen;
      }
      consumeToken(86);
      jjtree.closeNodeScope(astCollectionLiteral, true);
      open = false;
      astCollectionLiteral.lastToken = getToken(0);
      var image = token.image;
      astCollectionLiteral.type = image.substring(0, image.length() - 1);
      if (astCollectionLiteral.type == null || astCollectionLiteral.type.isBlank()) {
        astCollectionLiteral.type = "tuple";
      }
      if (isRange) {
        astCollectionLiteral.type = "range";
      }
      astCollectionLiteral.isComprehension = isComprehension;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astCollectionLiteral);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astCollectionLiteral, true);
        astCollectionLiteral.lastToken = getToken(0);
      }
    }
  }

  void Reference() {
    var astReference = new Reference();
    var open = jjtree.openNodeScope(astReference);
    astReference.firstToken = getToken(1);
    try {
      consumeToken(IDENTIFIER);
      jjtree.closeNodeScope(astReference, true);
      open = false;
      astReference.lastToken = getToken(0);
      astReference.name = astReference.firstToken.image;
    } finally {
      if (open) {
        jjtree.closeNodeScope(astReference, true);
        astReference.lastToken = getToken(0);
      }
    }
  }

  DestructuringAssignment DestructuringAssignment() {
    var astDectructuringAssignment = new DestructuringAssignment();
    var open = jjtree.openNodeScope(astDectructuringAssignment);
    astDectructuringAssignment.firstToken = getToken(1);
    Token varArgsToken = null;
    try {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case LET -> {
          consumeToken(LET);
          var names = DestructuredNames();
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case Ellipsis -> varArgsToken = consumeToken(Ellipsis);
            default -> la1[96] = gen;
          }
          consumeToken(Equals);
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case NEWLINE, COMMENT -> BlankLine();
            default -> la1[97] = gen;
          }
          ExpressionStatement();
          jjtree.closeNodeScope(astDectructuringAssignment, true);
          open = false;
          astDectructuringAssignment.lastToken = getToken(0);
          astDectructuringAssignment.type = LetOrVar.Type.LET;
          astDectructuringAssignment.names = names;
          astDectructuringAssignment.isVarargs = (varArgsToken != null);
          return astDectructuringAssignment;
        }
        case VAR -> {
          consumeToken(VAR);
          var names = DestructuredNames();
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case Ellipsis -> varArgsToken = consumeToken(Ellipsis);
            default -> la1[98] = gen;
          }
          consumeToken(Equals);
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case NEWLINE, COMMENT -> BlankLine();
            default -> la1[99] = gen;
          }
          ExpressionStatement();
          jjtree.closeNodeScope(astDectructuringAssignment, true);
          open = false;
          astDectructuringAssignment.lastToken = getToken(0);
          astDectructuringAssignment.type = LetOrVar.Type.VAR;
          astDectructuringAssignment.names = names;
          astDectructuringAssignment.isVarargs = (varArgsToken != null);
          return astDectructuringAssignment;
        }
        case IDENTIFIER -> {
          var names = DestructuredNames();
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case Ellipsis -> varArgsToken = consumeToken(Ellipsis);
            default -> la1[100] = gen;
          }
          consumeToken(Equals);
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case NEWLINE, COMMENT -> BlankLine();
            default -> la1[101] = gen;
          }
          ExpressionStatement();
          jjtree.closeNodeScope(astDectructuringAssignment, true);
          open = false;
          astDectructuringAssignment.lastToken = getToken(0);
          astDectructuringAssignment.type = null;
          astDectructuringAssignment.names = names;
          astDectructuringAssignment.isVarargs = (varArgsToken != null);
          return astDectructuringAssignment;
        }
        default -> {
          la1[102] = gen;
          consumeToken(-1);
          throw new ParseException();
        }
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astDectructuringAssignment);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astDectructuringAssignment, true);
        astDectructuringAssignment.lastToken = getToken(0);
      }
    }
  }

  List<String> DestructuredNames() {
    var names = new ArrayList<String>();
    var rootToken = consumeToken(IDENTIFIER);
    names.add(rootToken.image);
    loop: for(;;) {
      consumeToken(Comma);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case NEWLINE, COMMENT -> BlankLine();
        default -> la1[103] = gen;
      }
      var nextToken = consumeToken(IDENTIFIER);
      names.add(nextToken.image);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case Comma -> {}
        default -> { la1[104] = gen; break loop; }
      }
    }
    return names;
  }

  LetOrVar LetOrVar() {
    var astLetOrVar = new LetOrVar();
    var open = jjtree.openNodeScope(astLetOrVar);
    astLetOrVar.firstToken = getToken(1);
    try {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case LET -> {
          consumeToken(LET);
          var idToken = consumeToken(IDENTIFIER);
          consumeToken(Equals);
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case NEWLINE, COMMENT -> BlankLine();
            default ->  la1[105] = gen;
          }
          ExpressionStatement();
          jjtree.closeNodeScope(astLetOrVar, true);
          open = false;
          astLetOrVar.lastToken = getToken(0);
          astLetOrVar.type = LetOrVar.Type.LET;
          astLetOrVar.name = idToken.image;
          return astLetOrVar;
        }
        case VAR -> {
          consumeToken(VAR);
          var idToken = consumeToken(IDENTIFIER);
          consumeToken(Equals);
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case NEWLINE, COMMENT -> BlankLine();
            default -> la1[106] = gen;
          }
          ExpressionStatement();
          jjtree.closeNodeScope(astLetOrVar, true);
          open = false;
          astLetOrVar.lastToken = getToken(0);
          astLetOrVar.type = LetOrVar.Type.VAR;
          astLetOrVar.name = idToken.image;
          return astLetOrVar;
        }
        default -> {
          la1[107] = gen;
          consumeToken(-1);
          throw new ParseException();
        }
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astLetOrVar);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astLetOrVar, true);
        astLetOrVar.lastToken = getToken(0);
      }
    }
  }

  void Assignment() {
    var astAssignment = new Assignment();
    var open = jjtree.openNodeScope(astAssignment);
    astAssignment.firstToken = getToken(1);
    try {
      var idToken = consumeToken(IDENTIFIER);
      consumeToken(Equals);
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case NEWLINE, COMMENT -> BlankLine();
        default -> la1[108] = gen;
      }
      ExpressionStatement();
      jjtree.closeNodeScope(astAssignment, true);
      open = false;
      astAssignment.lastToken = getToken(0);
      astAssignment.name = idToken.image;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astAssignment);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astAssignment, true);
        astAssignment.lastToken = getToken(0);
      }
    }
  }

  void Return() {
    var astReturn = new Return();
    var open = jjtree.openNodeScope(astReturn);
    astReturn.firstToken = getToken(1);
    try {
      consumeToken(RETURN);
      if (TreeParser.this.aReturn(2)) {
        BlankLines();
        ExpressionStatement();
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astReturn);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astReturn, true);
        astReturn.lastToken = getToken(0);
      }
    }
  }

  void Argument() {
    var astArgument = new Argument();
    var open = jjtree.openNodeScope(astArgument);
    astArgument.firstToken = getToken(1);
    Token parameterId = null;
    try {
      if (TreeParser.this.anArgument(2)) {
        parameterId = consumeToken(IDENTIFIER);
        consumeToken(Equals);
      }
      ExpressionStatement();
      jjtree.closeNodeScope(astArgument, true);
      open = false;
      astArgument.lastToken = getToken(0);
      if (parameterId != null) {
        astArgument.name = parameterId.image;
        astArgument.isNamed = true;
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astArgument);
        open = false;
      } else {
        jjtree.popNode();
       }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astArgument, true);
        astArgument.lastToken = getToken(0);
      }
    }
  }

  void Arguments() {
    consumeToken(LeftParenthesis);
    switch ((ntk == -1) ? ntk_f() : ntk) {
      case NEWLINE, COMMENT -> BlankLine();
      default -> la1[109] = gen;
    }
    switch ((ntk == -1) ? ntk_f() : ntk) {
      case MATCH, UNARY_OPERATOR, MACRO_INVOCATION, NUMBER, LONG_NUMBER, BIGINTEGER, FLOATING_NUMBER, FLOAT, BIGDECIMAL, STRING, CHAR, NULL, TRUE, FALSE, CLASSREF, FUNREF, COLL_START, MULTI_STRING, IDENTIFIER, LeftBrace, LeftParenthesis, Bar, Arrow -> {
        Argument();
        loop: for(;;) {
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case Comma -> {}
            default -> { la1[110] = gen; break loop; }
          }
          consumeToken(Comma);
          switch ((ntk == -1) ? ntk_f() : ntk) {
            case NEWLINE, COMMENT -> BlankLine();
            default -> la1[111] = gen;
          }
          Argument();
        }
      }
      default -> la1[112] = gen;
    }
    consumeToken(RightParenthesis);
  }

  AnonymousFunctionInvocation AnonymousFunctionInvocation() {
    var astAnonymousFunctionInvocation = new AnonymousFunctionInvocation();
    var open = jjtree.openNodeScope(astAnonymousFunctionInvocation);
    astAnonymousFunctionInvocation.firstToken = getToken(1);
    Token constant = null;
    try {
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case CONSTANT_INVOCATION -> constant = consumeToken(CONSTANT_INVOCATION);
        default -> la1[113] = gen;
      }
      Arguments();
      jjtree.closeNodeScope(astAnonymousFunctionInvocation, true);
      open = false;
      astAnonymousFunctionInvocation.lastToken = getToken(0);
      if (constant != null) {
        astAnonymousFunctionInvocation.isConstant = true;
      }
      return astAnonymousFunctionInvocation;
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astAnonymousFunctionInvocation);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astAnonymousFunctionInvocation, true);
        astAnonymousFunctionInvocation.lastToken = getToken(0);
      }
    }
  }

  void FunctionInvocation() {
    var astFunctionInvocation = new FunctionInvocation();
    var open = jjtree.openNodeScope(astFunctionInvocation);
    astFunctionInvocation.firstToken = getToken(1);
    Token constant = null;
    try {
      var name = QualifiedName();
      switch ((ntk == -1) ? ntk_f() : ntk) {
        case CONSTANT_INVOCATION -> constant = consumeToken(CONSTANT_INVOCATION);
        default -> la1[114] = gen;
      }
      Arguments();
      while (isAnonymousFunctionInvocation(2)) {
        AnonymousFunctionInvocation();
      }
      jjtree.closeNodeScope(astFunctionInvocation, true);
      open = false;
      astFunctionInvocation.lastToken = getToken(0);
      astFunctionInvocation.name = name;
      if (constant != null) {
        astFunctionInvocation.isConstant = true;
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astFunctionInvocation);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astFunctionInvocation, true);
        astFunctionInvocation.lastToken = getToken(0);
      }
    }
  }

  void ConditionalBranching() {
    var astConditionalBranching = new ConditionalBranching();
    var open = jjtree.openNodeScope(astConditionalBranching);
    astConditionalBranching.firstToken = getToken(1);
    try {
      consumeToken(IF);
      ExpressionStatement();
      Block();
      if (aConditionalBranching(MAX_VALUE)) {
        consumeToken(ELSE);
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case IF -> {
            ConditionalBranching();
          }
          case LeftBrace -> {
            Block();
          }
          default -> {
            la1[115] = gen;
            consumeToken(-1);
            throw new ParseException();
          }
        }
      }
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astConditionalBranching);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astConditionalBranching, true);
        astConditionalBranching.lastToken = getToken(0);
      }
    }
  }

  void Case() {
    var astCase = new Case();
    var open = jjtree.openNodeScope(astCase);
    astCase.firstToken = getToken(1);
    try {
      consumeToken(CASE);
      consumeToken(LeftBrace);
      BlankLines();
      loop: for(;;) {
        consumeToken(WHEN);
        ExpressionStatement();
        Block();
        BlankLines();
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case WHEN -> {}
          default -> { la1[116] = gen; break loop; }
        }
      }
      consumeToken(OTHERWISE);
      Block();
      BlankLines();
      consumeToken(RightBrace);
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astCase);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astCase, true);
        astCase.lastToken = getToken(0);
      }
    }
  }

  void Match() {
    var astMatch = new Match();
    var open = jjtree.openNodeScope(astMatch);
    astMatch.firstToken = getToken(1);
    try {
      consumeToken(MATCH);
      consumeToken(LeftBrace);
      BlankLines();
      loop: for(;;) {
        consumeToken(WHEN);
        BlankLines();
        ExpressionStatement();
        BlankLines();
        consumeToken(THEN);
        BlankLines();
        ExpressionStatement();
        BlankLines();
        switch ((ntk == -1) ? ntk_f() : ntk) {
          case WHEN -> {}
          default -> { la1[117] = gen; break loop; }
        }
      }
      consumeToken(OTHERWISE);
      BlankLines();
      ExpressionStatement();
      BlankLines();
      consumeToken(RightBrace);
    } catch (Throwable t) {
      if (open) {
        jjtree.clearNodeScope(astMatch);
        open = false;
      } else {
        jjtree.popNode();
      }
      throw uncheck(t);
    } finally {
      if (open) {
        jjtree.closeNodeScope(astMatch, true);
        astMatch.lastToken = getToken(0);
      }
    }
  }


  boolean isBlankLine(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isBlankLine()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(0, xla); }
  }

  boolean isInvocationExpression(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isInvocationExpression()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(1, xla); }
  }

  boolean aDecoratorReference(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!scanToken(IDENTIFIER)); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(2, xla); }
  }

  boolean isAssignmentStatement(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isAssignment()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(3, xla); }
  }

  boolean isDestructuringAssignment(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isDestructuringAssignment()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(4, xla); }
  }

  boolean isLetOrVar(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isLetOrVar()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(5, xla); }
  }

  boolean aForLoopBlock(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isBlock()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(6, xla); }
  }

  boolean aForEachLoop(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!aForEachLoop()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(7, xla); }
  }

  boolean aForEachLoopDestructuredNames(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!aForEachLoopDestructuredNames()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(8, xla); }
  }

  boolean aForEaachLoopBlock(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isBlock()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(9, xla); }
  }

  boolean isLocalDeclaration(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isLocalDeclaration()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(10, xla); }
  }

  boolean isLocalAssignment(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isAssignment()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(11, xla); }
  }

  boolean isFunctionInvocation(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isFunctionInvocation()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(12, xla); }
  }

  boolean anAtomReference(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!scanToken(IDENTIFIER)); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(13, xla); }
  }

  boolean anInvocationExpression(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!anInvocationExpression()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(14, xla); }
  }

  boolean isAnonymousInvocationExpression(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isAnonymousFunctionInvocation()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(15, xla); }
  }

  boolean aMultiplicativeExpression(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!aMultiplicativeExpression()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(16, xla); }
  }

  boolean anAdditiveExpression(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!anAdditiveExpression()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(17, xla); }
  }

  boolean aRelationalExpression(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!aRelationalExpression()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(18, xla); }
  }

  boolean anEqualityExpression(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!anEqualityExpression()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(19, xla); }
  }

  boolean anAndExpression(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!anAndExpression()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(20, xla); }
  }

  boolean anOrExpression(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!anOrExpression()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(21, xla); }
  }

  boolean anOrIfNullExpression(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!anOrIfNullExpression()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(22, xla); }
  }

  boolean isAnonymousMethodInvocation(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isAnonymousFunctionInvocation()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(23, xla); }
  }

  boolean aMacroInvocationArguments(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isArguments()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(24, xla); }
  }

  boolean aMacroInvocation(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!aMacroInvocation()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(25, xla); }
  }

  boolean aReturn(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!aReturn()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(26, xla); }
  }

  boolean anArgument(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!anArgument()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(27, xla); }
  }

  boolean isAnonymousFunctionInvocation(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!isAnonymousFunctionInvocation()); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(28, xla); }
  }

  boolean aConditionalBranching(int xla) {
    la = xla;
    lastPos = scanPos = ct;
    try { return (!scanToken(ELSE)); }
    catch (LookaheadSuccess x) { return true; }
    finally { save(29, xla); }
  }

  boolean isParenthesisDesctructuredNames() {
    return scanToken(LeftParenthesis)|| isDestructuredNames();
  }


  boolean aForEachLoopDestructuredNames() {
    var xsp = scanPos;
    if (isDestructuredNames()) {
      scanPos = xsp;
      if (isParenthesisDesctructuredNames()) {
        return true;
      }
    }
    return false;
  }

  boolean isParenthesisIdentifier() {
    return scanToken(LeftParenthesis) || scanToken(IDENTIFIER);
  }


  boolean isIdentifierIn() {
    return scanToken(IDENTIFIER) || scanToken(IN);
  }

  boolean aForEachLoop() {
    var xsp = scanPos;
    if (isIdentifierIn()) {
      scanPos = xsp;
      if (isParenthesisIdentifier()) {
        return true;
      }
    }
    return false;
  }

  boolean isCollectionLiteral() {
    if (scanToken(COLL_START)) {
      return true;
    }
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    xsp = scanPos;
    if (isExpressionStatement()) {
      scanPos = xsp;
    }
    return scanToken(RightBracket);
  }

  boolean isStatement() {
    var xsp = scanPos;
    if (isBlankLine()) { scanPos = xsp;
    if (isAssignment()) { scanPos = xsp;
    if (isDestructuringAssignment()) { scanPos = xsp;
    if (isLetOrVar()) { scanPos = xsp;
    if (isExpressionStatement()) { scanPos = xsp;
    if (scanToken(RETURN)) { scanPos = xsp;
    if (scanToken(IF)) { scanPos = xsp;
    if (scanToken(WHILE)) { scanPos = xsp;
    if (scanToken(FOR)) { scanPos = xsp;
    if (scanToken(FOREACH)) { scanPos = xsp;
    if (scanToken(THROW)) { scanPos = xsp;
    if (scanToken(TRY)) { scanPos = xsp;
    if (scanToken(CASE)) { scanPos = xsp;
    if (scanToken(CONTINUE)) { scanPos = xsp;
    if (scanToken(BREAK)) {
      return true;
    }}}}}}}}}}}}}}}
    return false;
  }

  boolean isLiteral() {
    var xsp = scanPos;
    if (scanToken(CLASSREF)) { scanPos = xsp;
    if (isStringLiteral()) { scanPos = xsp;
    if (scanToken(CHAR)) { scanPos = xsp;
    if (scanToken(BIGDECIMAL)) { scanPos = xsp;
    if (scanToken(BIGINTEGER)) { scanPos = xsp;
    if (scanToken(FLOAT)) { scanPos = xsp;
    if (scanToken(FLOATING_NUMBER)) { scanPos = xsp;
    if (scanToken(LONG_NUMBER)) { scanPos = xsp;
    if (scanToken(NUMBER)) { scanPos = xsp;
    if (scanToken(NULL)) { scanPos = xsp;
    if (scanToken(TRUE)) { scanPos = xsp;
    if (scanToken(FALSE)) { scanPos = xsp;
    if (scanToken(FUNREF)) {
      return true;
    }}}}}}}}}}}}}
    return false;
  }

  boolean isDecoratorDeclaration() {
    if (scanToken(DECORATOR)) {
      return true;
    }
    var xsp = scanPos;
    if (scanToken(43)) {
      scanPos = xsp;
    }
    xsp = scanPos;
    if (isInvocationExpression()) {
      scanPos = xsp;
      if (scanToken(IDENTIFIER)) {
        return true;
      }
    }
    return isBlankLines();
  }

  boolean isArrowExpression() {
    if (scanToken(Arrow)) {
      return true;
    }
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return isExpressionStatement();
  }

  boolean isBarIdentifier() {
    return scanToken(Bar) || scanToken(IDENTIFIER);
  }

  boolean isFunction() {
    var xsp = scanPos;
    if (isBarIdentifier()) {
      scanPos = xsp;
    }
    xsp = scanPos;
    if (isBlock()) {
      scanPos = xsp;
      if (isArrowExpression()) {
        return true;
      }
    }
    return false;
  }

  boolean isFunctionDeclaration() {
    var xsp = scanPos;
    if (scanToken(FUNCTION)) {
      scanPos = xsp;
      if (scanToken(MACRO)) {
        return true;
      }
    }
    return scanToken(IDENTIFIER) || scanToken(Equals);
  }

  boolean isTopLevelDeclarationBlankLines() {
    return isTopLevelMacro() || isBlankLines();
  }

  boolean isImportDeclarationBlankLines() {
    return isImportDeclaration() || isBlankLines();
  }

  boolean isImportTopLevel() {
    var xsp = scanPos;
    if (isImportDeclarationBlankLines()) {
      scanPos = xsp;
      if (isTopLevelDeclarationBlankLines()) {
        return true;
      }
    }
    return false;
  }

  boolean isMacroInvocationBlock() {
    var xsp = scanPos;
    if (aMacroInvocation()) {
      scanPos = xsp;
      if (isBlock()) {
        return true;
      }
    }
    return false;
  }

  boolean isAugmentDeclaration() {
    if (scanToken(AUGMENT) || isQualifiedName()) {
      return true;
    }
    var xsp = scanPos;
    if (scanToken(LeftBrace)) {
      scanPos = xsp;
      if (scanToken(WITH)) {
        return true;
      }
    }
    return false;
  }

  boolean aMacroInvocation() {
    if (scanToken(LeftBrace) || isBlankLines() || isImportTopLevel()) {
      return true;
    }
    for(;;) {
      var xsp = scanPos;
      if (isImportTopLevel()) {
        scanPos = xsp;
        break;
      }
    }
    return isBlankLines() || scanToken(RightBrace);
  }


  boolean isMacroInvocation() {
    if (scanToken(MACRO_INVOCATION) || isQualifiedName()) {
      return true;
    }
    var xsp = scanPos;
    if (isArguments()) {
      scanPos = xsp;
    }
    xsp = scanPos;
    if (isMacroInvocationBlock()) {
      scanPos = xsp;
    }
    return false;
  }

  boolean isNamedAugmentationDeclaration() {
    return scanToken(NAMEDAUGMENTATION) || scanToken(IDENTIFIER) || scanToken(Equals);
  }

  boolean isBlock() {
    if (scanToken(LeftBrace)) {
      return true;
    }
    for(;;) {
      var xsp = scanPos;
      if (isStatement()) {
        scanPos = xsp;
        break;
      }
    }
    return scanToken(RightBrace);
  }

  boolean anOrIfNullExpression() {
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    if (scanToken(OR_IFNULL_OPERATOR)) {
      return true;
    }
    xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return isOrExpression();
  }

  boolean isOrIfNullExpression() {
    if (isOrExpression()) {
      return true;
    }
    for(;;) {
      var xsp = scanPos;
      if (anOrIfNullExpression()) {
        scanPos = xsp;
        break;
      }
    }
    return false;
  }

  boolean isMatch() {
    return scanToken(MATCH) || scanToken(LeftBrace);
  }

  boolean isStringLiteral() {
    var xsp = scanPos;
    if (scanToken(STRING)) {
      scanPos = xsp;
      if (scanToken(MULTI_STRING)) {
        return true;
      }
    }
    return false;
  }

  boolean isUnionDeclaration() {
    return scanToken(UNION) || scanToken(IDENTIFIER) || scanToken(Equals);
  }

  boolean anOrExpression() {
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    if (scanToken(OR_OPERATOR)) {
      return true;
    }
    xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return isAndExpression();
  }

  boolean isOrExpression() {
    if (isAndExpression()) {
      return true;
    }
    for(;;) {
      var xsp = scanPos;
      if (anOrExpression()) {
        scanPos = xsp;
        break;
      }
    }
    return false;
  }

  boolean anAndExpression() {
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    if (scanToken(AND_OPERATOR)) {
      return true;
    }
    xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return isEqualityExpression();
  }

  boolean isAndExpression() {
    if (isEqualityExpression()) {
      return true;
    }
    for(;;) {
      var xsp = scanPos;
      if (anAndExpression()) {
        scanPos = xsp;
        break;
      }
    }
    return false;
  }

  boolean isFunctionInvocation() {
    if (isQualifiedName()) {
      return true;
    }
    var xsp = scanPos;
    if (scanToken(43)) {
      scanPos = xsp;
    }
    return isArguments();
  }

  boolean anEqualityExpression() {
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    if (scanToken(EQUALITY_OPERATOR)) {
      return true;
    }
    xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return isRelationalExpression();
  }

  boolean isEqualityExpression() {
    if (isRelationalExpression()) {
      return true;
    }
    var xsp = scanPos;
    if (anEqualityExpression()) {
      scanPos = xsp;
    }
    return false;
  }

  boolean isStructDeclaration() {
    return scanToken(STRUCT) || scanToken(IDENTIFIER) || scanToken(Equals);
  }

  boolean isAnonymousFunctionInvocation() {
    var xsp = scanPos;
    if (scanToken(43)) {
      scanPos = xsp;
    }
    return isArguments();
  }


  boolean aRelationalExpression() {
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    if (scanToken(RELATIONAL_OPERATOR)) {
      return true;
    }
    xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return isAdditiveExpression();
  }


  boolean isRelationalExpression() {
    if (isAdditiveExpression()) {
      return true;
    }
    var xsp = scanPos;
    if (aRelationalExpression()) {
      scanPos = xsp;
    }
    return false;
  }

  boolean isArguments() {
    if (scanToken(LeftParenthesis)) {
      return true;
    }
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    xsp = scanPos;
    if (isArgument()) {
      scanPos = xsp;
    }
    return scanToken(RightParenthesis);
  }

  boolean anAdditiveExpression() {
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    if (scanToken(ADDITIVE_OPERATOR)) {
      return true;
    }
    xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return isMultiplicativeExpression();
  }

  boolean aReturn() {
    return isBlankLines() || isExpressionStatement();
  }

  boolean anArgument() {
    return scanToken(IDENTIFIER) || scanToken(Equals);
  }

  boolean isQualifiedName() {
    if (scanToken(IDENTIFIER)) {
      return true;
    }
    for(;;) {
      var xsp = scanPos;
      if (scanToken(Dot)) {
        scanPos = xsp;
        break;
      }
    }
    return false;
  }

  boolean isArgument() {
    var xsp = scanPos;
    if (anArgument()) {
      scanPos = xsp;
    }
    return isExpressionStatement();
  }

  boolean isAdditiveExpression() {
    if (isMultiplicativeExpression()) {
      return true;
    }
    for(;;) {
      var xsp = scanPos;
      if (anAdditiveExpression()) {
        scanPos = xsp;
        break;
      }
    }
    return false;
  }

  boolean isBlankLines() {
    for(;;) {
      var xsp = scanPos;
      if (isBlankLine()) {
        scanPos = xsp;
        break;
      }
    }
    return false;
  }


  boolean aMultiplicativeExpression() {
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    if (scanToken(MULTIPLICATIVE_OPERATOR)) {
      return true;
    }
    xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return isInvocationExpression();
  }

  boolean isBlankLine() {
    var xsp = scanPos;
    if (scanToken(NEWLINE)) {
      scanPos = xsp;
      if (scanToken(COMMENT)) {
        return true;
      }
    }
    return false;
  }

  boolean isMultiplicativeExpression() {
    if (isUnaryExpression()) {
      return true;
    }
    for(;;) {
      var xsp = scanPos;
      if (aMultiplicativeExpression()) {
        scanPos = xsp;
        break;
      }
    }
    return false;
  }


  boolean isAssignment() {
    if (scanToken(IDENTIFIER) || scanToken(Equals)) {
      return true;
    }
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return isExpressionStatement();
  }

  boolean isVarIdentifier() {
    return scanToken(VAR) || scanToken(IDENTIFIER);
  }

  boolean anInvocationExpression() {
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    if (scanToken(INVOCATION_OPERATOR)) {
      return true;
    }
    xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return scanToken(IDENTIFIER);
  }

  boolean isLocalFunctionDeclaration() {
    var xsp = scanPos;
    if (scanToken(LOCAL)) {
      scanPos = xsp;
    }
    return isFunctionDeclaration();
  }

  boolean isInvocationExpression() {
    if (isAtom()) {
      return true;
    }
    Token xsp;
    for(;;) {
      xsp = scanPos;
      if (anInvocationExpression()) {
        scanPos = xsp;
        break;
      }
    }
    for(;;) {
      xsp = scanPos;
      if (isAnonymousFunctionInvocation()) {
        scanPos = xsp;
        break;
      }
    }
    return false;
  }

  boolean isLetOrVar() {
    var xsp = scanPos;
    if (isLetIdentifier()) {
      scanPos = xsp;
      if (isVarIdentifier()) {
        return true;
      }
    }
    return false;
  }

  boolean isLetIdentifier() {
    return scanToken(LET) || scanToken(IDENTIFIER);
  }

  boolean isTopLevelMacro() {
    var xsp = scanPos;
    if (isToplevelDeclaration()) {
      scanPos = xsp;
      if (isMacroInvocation()) {
        return true;
      }
    }
    return false;
  }

  boolean isToplevelDeclaration() {
    var xsp = scanPos;
    if (scanToken(DOCUMENTATION)) {
      scanPos = xsp;
    }
    for(;;) {
      xsp = scanPos;
      if (isDecoratorDeclaration()) {
        scanPos = xsp;
        break;
      }
    }
    xsp = scanPos;
    if (isLocalFunctionDeclaration()) {
      scanPos = xsp;
      if (isStructDeclaration()) {
        scanPos = xsp;
        if (isUnionDeclaration()) {
          scanPos = xsp;
          if (isAugmentDeclaration()) {
            scanPos = xsp;
            if (isNamedAugmentationDeclaration()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  boolean isUnaryExpression() {
    var xsp = scanPos;
    if (scanToken(41)) {
      scanPos = xsp;
    }
    return isInvocationExpression();
  }

  boolean isMoreDesctucturedNames() {
    if (scanToken(Comma)) {
      return true;
    }
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return scanToken(IDENTIFIER);
  }

  boolean isParenthesisExpression() {
    if (scanToken(LeftParenthesis)) {
      return true;
    }
    var xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return isExpressionStatement();
  }

  boolean isDestructuredNames() {
    if (scanToken(IDENTIFIER)) {
      return true;
    }
    if (isMoreDesctucturedNames()) {
      return true;
    }
    for(;;) {
      var xsp = scanPos;
      if (isMoreDesctucturedNames()) {
        scanPos = xsp;
        break;
      }
    }
    return false;
  }

  boolean isImportDeclaration() {
    if (scanToken(IMPORT)) {
      return true;
    }
    var xsp = scanPos;
    if (scanToken(Dot)) {
      scanPos = xsp;
    }
    if (isQualifiedName()) {
      return true;
    }
    xsp = scanPos;
    if (scanToken(DotBrace)) {
      scanPos = xsp;
    }
    return false;
  }

  boolean isAtom() {
    var xsp = scanPos;
    if (isFunctionInvocation()) { scanPos = xsp;
    if (scanToken(IDENTIFIER)) { scanPos = xsp;
    if (isMacroInvocation()) { scanPos = xsp;
    if (isMatch()) { scanPos = xsp;
    if (isLiteral()) { scanPos = xsp;
    if (isFunction()) { scanPos = xsp;
    if (isCollectionLiteral()) { scanPos = xsp;
    if (isParenthesisExpression()) {
      return true;
    }}}}}}}}
    return false;
  }

  boolean isVarDestructuredNames() {
    return scanToken(VAR) || isDestructuredNames();
  }

  boolean isLocalDeclaration() {
    return scanToken(WITH) || scanToken(LeftBrace);
  }

  boolean isExpressionStatement() {
    if (isOrIfNullExpression()) {
      return true;
    }
    var xsp = scanPos;
    if (isLocalDeclaration()) {
      scanPos = xsp;
    }
    xsp = scanPos;
    if (isBlankLine()) {
      scanPos = xsp;
    }
    return false;
  }

  boolean isDestructuringAssignment() {
    var xsp = scanPos;
    if (isLetDestructuredNames()) { scanPos = xsp;
    if (isVarDestructuredNames()) { scanPos = xsp;
    if (isDestructuredNames()) {
      return true;
    }}}
    return false;
  }

  boolean isLetDestructuredNames() {
    return scanToken(LET) || isDestructuredNames();
  }






  /** Generated Token Manager. */
  TokenManager tokenSource;
  CharSource charSource;

  /** Current token. */
  Token ct;
  /** Next token. */
  Token nt;

  int ntk;
  Token scanPos, lastPos;

  int la;
  int gen;
  final int[] la1 = new int[118];

  static int[] la1_0 = new int[] {
      0x20, 0x0, 0x0, 0x20, 0x0, 0x20, 0x0, 0x100, 0x18000600, 0x18000600, 0xa0, 0x0, 0x0, 0x20,
      0x20, 0x0, 0x20, 0x0, 0x0, 0x0, 0x0, 0x400, 0x18000600, 0x18000600, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x200,
      0x0, 0x0, 0x200, 0x20000000, 0x200, 0x0, 0x20, 0xc14dd800, 0x20, 0x20, 0x20, 0x20, 0x800000, 0x800000, 0x0, 0x0,
      0x800000, 0x0, 0x800000, 0x0, 0x200000, 0x300000, 0x20, 0x20, 0x20, 0x20, 0x20, 0x1000000, 0x0, 0x20, 0x20,
      0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0xc14dd820, 0x18000700,
      0x18000700, 0x0, 0x0, 0x0, 0x0, 0x20, 0x0, 0x0, 0x20, 0x20, 0x0, 0x18000, 0x18000, 0x18000, 0x18000, 0x20,
      0x1000000, 0x0, 0x20, 0x0, 0x20, 0x0, 0x20, 0x0, 0x20, 0x0, 0x20, 0x20, 0x0, 0x20, 0x20, 0x0, 0x20, 0x1000000,
      0x0, 0x0, 0x1000, 0x800000, 0x800000,
  };

  static int[] la1_1 = new int[] {
      0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x800000, 0x0, 0x1f003, 0x1f003, 0x1000, 0x0, 0x1000, 0x0, 0x0,
      0x0, 0x0, 0x0, 0x0, 0x0, 0x1000, 0x0, 0x4003, 0x7003, 0x0, 0x0, 0x0, 0x1000, 0x0, 0x0, 0x1000, 0x7000, 0x0,
      0x1000, 0x7000, 0x0, 0x4000, 0x800, 0x0, 0x7ffe2200, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
      0x0, 0x0, 0x0, 0x18000, 0x18000, 0x0, 0x0, 0x7ffe2000, 0x200, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
      0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x7fffa200, 0x7003, 0x7003, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x3ffe0000, 0x0, 0x0,
      0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x7ffe2200, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x18000, 0x0, 0x0, 0x0, 0x0, 0x18000,
      0x0, 0x0, 0x0, 0x0, 0x7ffe2200, 0x800, 0x800, 0x0, 0x0, 0x0,
  };

  static int[] la1_2 = new int[] {
      0x20, 0x400, 0x800, 0x20, 0x800, 0x20, 0x2, 0x0, 0x100, 0x100, 0x120, 0x100, 0x0, 0x20, 0x20,
      0x800, 0x20, 0x400, 0x1000, 0x100, 0x0, 0x0, 0x0, 0x100, 0x100, 0x800, 0x100, 0x0, 0x800, 0x4000, 0x104, 0x100,
      0x100, 0x0, 0x100, 0x8000, 0x0, 0x0, 0x20, 0x198006, 0x20, 0x20, 0x20, 0x20, 0x0, 0x0, 0x10004, 0x40000, 0x0,
      0x40000, 0x0, 0x10004, 0x0, 0x0, 0x20, 0x24, 0x24, 0x20, 0x20, 0x198002, 0x0, 0x20, 0x20, 0x20, 0x20, 0x20,
      0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x198026, 0x100, 0x100, 0x8000, 0x8000,
      0x40000, 0x80000, 0x20, 0x108000, 0x2, 0x20, 0x20, 0x800, 0x0, 0x0, 0x200800, 0x200800, 0x20, 0x198006, 0x40000,
      0x20, 0x40000, 0x20, 0x40000, 0x20, 0x4, 0x20, 0x800, 0x20, 0x20, 0x0, 0x20, 0x20, 0x800, 0x20, 0x198006, 0x0,
      0x0, 0x8000, 0x0, 0x0,
  };

  final JJCalls[] rtns = new JJCalls[30];
  boolean rescan = false;
  int gc = 0;

  /** Constructor. */
  public TreeParser(java.io.Reader stream) {
    charSource = new CharSource(stream, 1, 1);
    tokenSource = new TokenManager(charSource);
    ct = new Token();
    ntk = -1;
    gen = 0;
    for (var i = 0; i < 118; i++) {
      la1[i] = -1;
    }
    for (var i = 0; i < rtns.length; i++) {
      rtns[i] = new JJCalls();
    }
  }

  public void reInit(java.io.Reader stream) {
    if (charSource == null) {
      charSource = new CharSource(stream, 1, 1);
    } else {
      charSource.ReInit(stream, 1, 1);
    }
    if (tokenSource == null) {
      tokenSource = new TokenManager(charSource);
    }

    tokenSource.reset(charSource);
    ct = new Token();
    ntk = -1;
    jjtree.reset();
    gen = 0;
    for (var i = 0; i < 118; i++) {
      la1[i] = -1;
    }
    for (var i = 0; i < rtns.length; i++) {
      rtns[i] = new JJCalls();
    }
  }

  TreeParser(TokenManager tm) {
    tokenSource = tm;
    ct = new Token();
    ntk = -1;
    gen = 0;
    for (var i = 0; i < 118; i++) {
      la1[i] = -1;
    }
    for (var i = 0; i < rtns.length; i++) {
      rtns[i] = new JJCalls();
    }
  }

  void reInit(TokenManager tm) {
    tokenSource = tm;
    ct = new Token();
    ntk = -1;
    jjtree.reset();
    gen = 0;
    for (var i = 0; i < 118; i++) {
      la1[i] = -1;
    }
    for (var i = 0; i < rtns.length; i++) {
      rtns[i] = new JJCalls();
    }
  }

  Token consumeToken(int kind) {
    Token oldToken;
    if ((oldToken = ct).next != null) {
      ct = ct.next;
    } else {
      ct = ct.next = tokenSource.getNextToken();
    }
    ntk = -1;
    if (ct.kind == kind) {
      gen++;
      if (++gc > 100) {
        gc = 0;
        for (int i = 0; i < rtns.length; i++) {
          JJCalls c = rtns[i];
          while (c != null) {
            if (c.gen < gen) {
              c.first = null;
            }
            c = c.next;
          }
        }
      }
      return ct;
    }
    ct = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static class LookaheadSuccess extends Error {
    @Override public Throwable fillInStackTrace() { return this; }
  }

  static final LookaheadSuccess ls = new LookaheadSuccess();

  boolean scanToken(int kind) {
    if (scanPos == lastPos) {
      la--;
      if (scanPos.next == null) {
        lastPos = scanPos = scanPos.next = tokenSource.getNextToken();
      } else {
        lastPos = scanPos = scanPos.next;
      }
    } else {
      scanPos = scanPos.next;
    }
    if (rescan) {
      int i = 0;
      Token tok = ct;
      while (tok != null && tok != scanPos) {
        i++;
        tok = tok.next;
      }
      if (tok != null) {
        addErrorToken(kind, i);
      }
    }
    if (scanPos.kind != kind) {
      return true;
    }
    if (la == 0 && scanPos == lastPos) {
      throw ls;
    }
    return false;
  }

  /** Get the next Token. */
  Token getNextToken() {
    if (ct.next != null) {
      ct = ct.next;
    } else {
      ct = ct.next = tokenSource.getNextToken();
    }
    ntk = -1;
    gen++;
    return ct;
  }

  /** Get the specific Token. */
  Token getToken(int index) {
    Token t = ct;
    for (int i = 0; i < index; i++) {
      if (t.next != null) {
        t = t.next;
      } else {
        t = t.next = tokenSource.getNextToken();
      }
    }
    return t;
  }

  int ntk_f() {
    if ((nt = ct.next) == null) {
      return (ntk = (ct.next = tokenSource.getNextToken()).kind);
    } else {
      return (ntk = nt.kind);
    }
  }

  java.util.List<int[]> jj_expentries = new java.util.ArrayList<>();
  int[] jj_expentry;
  int jj_kind = -1;
  int[] jj_lasttokens = new int[100];
  int jj_endpos;

  void addErrorToken(int kind, int pos) {
    if (pos >= 100) {
      return;
    }

    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];

      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }

      for (int[] oldentry : jj_expentries) {
        if (oldentry.length == jj_expentry.length) {
          boolean isMatched = true;

          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              isMatched = false;
              break;
            }

          }
          if (isMatched) {
            jj_expentries.add(jj_expentry);
            break;
          }
        }
      }

      if (pos != 0) {
        jj_lasttokens[(jj_endpos = pos) - 1] = kind;
      }
    }
  }

  /** Generate ParseException. */
  ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[87];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 118; i++) {
      if (la1[i] == gen) {
        for (int j = 0; j < 32; j++) {
          if ((la1_0[i] & (1 << j)) != 0) {
            la1tokens[j] = true;
          }
          if ((la1_1[i] & (1 << j)) != 0) {
            la1tokens[32 + j] = true;
          }
          if ((la1_2[i] & (1 << j)) != 0) {
            la1tokens[64 + j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 87; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    rescanToken();
    addErrorToken(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(ct, exptokseq, tokenImage);
  }

  boolean trace_enabled;

  /** Trace enabled. */
  boolean trace_enabled() { return trace_enabled; }

  /** Enable tracing. */
  void enable_tracing() {}

  /** Disable tracing. */
  void disable_tracing() {}

  void rescanToken() {
    rescan = true;
    for (var i = 0; i < 30; i++) {
      try {
        var p = rtns[i];
        do {
          if (p.gen > gen) {
            la = p.arg;
            lastPos = scanPos = p.first;
            switch (i) {
              case  0 -> isBlankLine();
              case  1 -> isInvocationExpression();
              case  2 -> scanToken(IDENTIFIER);
              case  3 -> isAssignment();
              case  4 -> isDestructuringAssignment();
              case  5 -> isLetOrVar();
              case  6 -> isBlock();
              case  7 -> aForEachLoop();
              case  8 -> aForEachLoopDestructuredNames();
              case  9 -> isBlock();
              case 10 -> isLocalDeclaration();
              case 11 -> isAssignment();
              case 12 -> isFunctionInvocation();
              case 13 -> scanToken(IDENTIFIER);
              case 14 -> anInvocationExpression();
              case 15 -> isAnonymousFunctionInvocation();
              case 16 -> aMultiplicativeExpression();
              case 17 -> anAdditiveExpression();
              case 18 -> aRelationalExpression();
              case 19 -> anEqualityExpression();
              case 20 -> anAndExpression();
              case 21 -> anOrExpression();
              case 22 -> anOrIfNullExpression();
              case 23 -> isAnonymousFunctionInvocation();
              case 24 -> isArguments();
              case 25 -> aMacroInvocation();
              case 26 -> aReturn();
              case 27 -> anArgument();
              case 28 -> isAnonymousFunctionInvocation();
              case 29 -> scanToken(ELSE);
            }
          }
          p = p.next;
        } while (p != null);

      } catch (LookaheadSuccess x) {}
    }
    rescan = false;
  }

  void save(int index, int xla) {
    JJCalls p = rtns[index];
    while (p.gen > gen) {
      if (p.next == null) {
        p = p.next = new JJCalls();
        break;
      }
      p = p.next;
    }
    p.gen = gen + xla - la;
    p.first = ct;
    p.arg = xla;
  }

  class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

  @SuppressWarnings("unchecked")
  static <T extends Throwable> T uncheck(Throwable t) throws T { throw (T)t; }
}
