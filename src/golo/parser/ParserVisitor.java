package golo.parser;

import golo.parser.ast.*;

public interface ParserVisitor {

  default <T, N extends Node> T visitAll(N node, T data) {
    if (node.children != null) for (var n:node.children) if (n != null) this.visit(node, data);
    return data;
  }

  default <T> T visit(Node node, T data) { return visitAll(node, data); }
  default <T> T visit(ParseError node, T data) { return visitAll(node, data); }
  default <T> T visit(CompilationUnit node, T data) { return visitAll(node, data); }
  default <T> T visit(ModuleDeclaration node, T data) { return visitAll(node, data); }
  default <T> T visit(ImportDeclaration node, T data) { return visitAll(node, data); }
  default <T> T visit(TopLevelDeclaration node, T data) { return visitAll(node, data); }
  default <T> T visit(MemberDeclaration node, T data) { return visitAll(node, data); }
  default <T> T visit(StructDeclaration node, T data) { return visitAll(node, data); }
  default <T> T visit(UnionValue node, T data) { return visitAll(node, data); }
  default <T> T visit(UnionDeclaration node, T data) { return visitAll(node, data); }
  default <T> T visit(NamedAugmentationDeclaration node, T data) { return visitAll(node, data); }
  default <T> T visit(AugmentDeclaration node, T data) { return visitAll(node, data); }
  default <T> T visit(FunctionDeclaration node, T data) { return visitAll(node, data); }
  default <T> T visit(DecoratorDeclaration node, T data) { return visitAll(node, data); }
  default <T> T visit(Continue node, T data) { return visitAll(node, data); }
  default <T> T visit(Break node, T data) { return visitAll(node, data); }
  default <T> T visit(Throw node, T data) { return visitAll(node, data); }
  default <T> T visit(WhileLoop node, T data) { return visitAll(node, data); }
  default <T> T visit(ForLoop node, T data) { return visitAll(node, data); }
  default <T> T visit(ForEachLoop node, T data) { return visitAll(node, data); }
  default <T> T visit(TryCatchFinally node, T data) { return visitAll(node, data); }
  default <T> T visit(ExpressionStatement node, T data) { return visitAll(node, data); }
  default <T> T visit(LocalDeclaration node, T data) { return visitAll(node, data); }
  default <T> T visit(UnaryExpression node, T data) { return visitAll(node, data); }
  default <T> T visit(InvocationExpression node, T data) { return visitAll(node, data); }
  default <T> T visit(MultiplicativeExpression node, T data) { return visitAll(node, data); }
  default <T> T visit(AdditiveExpression node, T data) { return visitAll(node, data); }
  default <T> T visit(RelationalExpression node, T data) { return visitAll(node, data); }
  default <T> T visit(EqualityExpression node, T data) { return visitAll(node, data); }
  default <T> T visit(AndExpression node, T data) { return visitAll(node, data); }
  default <T> T visit(OrExpression node, T data) { return visitAll(node, data); }
  default <T> T visit(OrIfNullExpression node, T data) { return visitAll(node, data); }
  default <T> T visit(MethodInvocation node, T data) { return visitAll(node, data); }
  default <T> T visit(Block node, T data) { return visitAll(node, data); }
  default <T> T visit(MacroInvocation node, T data) { return visitAll(node, data); }
  default <T> T visit(Function node, T data) { return visitAll(node, data); }
  default <T> T visit(Literal node, T data) { return visitAll(node, data); }
  default <T> T visit(CollectionLiteral node, T data) { return visitAll(node, data); }
  default <T> T visit(Reference node, T data) { return visitAll(node, data); }
  default <T> T visit(DestructuringAssignment node, T data) { return visitAll(node, data); }
  default <T> T visit(LetOrVar node, T data) { return visitAll(node, data); }
  default <T> T visit(Assignment node, T data) { return visitAll(node, data); }
  default <T> T visit(Return node, T data) { return visitAll(node, data); }
  default <T> T visit(Argument node, T data) { return visitAll(node, data); }
  default <T> T visit(AnonymousFunctionInvocation node, T data) { return visitAll(node, data); }
  default <T> T visit(FunctionInvocation node, T data) { return visitAll(node, data); }
  default <T> T visit(ConditionalBranching node, T data) { return visitAll(node, data); }
  default <T> T visit(Case node, T data) { return visitAll(node, data); }
  default <T> T visit(Match node, T data) { return visitAll(node, data); }
}
