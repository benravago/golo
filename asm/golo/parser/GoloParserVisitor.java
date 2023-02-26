package golo.parser;

import golo.parser.ast.*;

public interface GoloParserVisitor {

  Object visit(SimpleNode node, Object data);

  Object visit(ParseError node, Object data);

  Object visit(CompilationUnit node, Object data);

  Object visit(ModuleDeclaration node, Object data);

  Object visit(ImportDeclaration node, Object data);

  Object visit(ToplevelDeclaration node, Object data);

  Object visit(MemberDeclaration node, Object data);

  Object visit(StructDeclaration node, Object data);

  Object visit(UnionValue node, Object data);

  Object visit(UnionDeclaration node, Object data);

  Object visit(NamedAugmentationDeclaration node, Object data);

  Object visit(AugmentDeclaration node, Object data);

  Object visit(FunctionDeclaration node, Object data);

  Object visit(DecoratorDeclaration node, Object data);

  Object visit(Continue node, Object data);

  Object visit(Break node, Object data);

  Object visit(Throw node, Object data);

  Object visit(WhileLoop node, Object data);

  Object visit(ForLoop node, Object data);

  Object visit(ForEachLoop node, Object data);

  Object visit(TryCatchFinally node, Object data);

  Object visit(ExpressionStatement node, Object data);

  Object visit(LocalDeclaration node, Object data);

  Object visit(UnaryExpression node, Object data);

  Object visit(InvocationExpression node, Object data);

  Object visit(MultiplicativeExpression node, Object data);

  Object visit(AdditiveExpression node, Object data);

  Object visit(RelationalExpression node, Object data);

  Object visit(EqualityExpression node, Object data);

  Object visit(AndExpression node, Object data);

  Object visit(OrExpression node, Object data);

  Object visit(OrIfNullExpression node, Object data);

  Object visit(MethodInvocation node, Object data);

  Object visit(Block node, Object data);

  Object visit(MacroInvocation node, Object data);

  Object visit(Function node, Object data);

  Object visit(Literal node, Object data);

  Object visit(CollectionLiteral node, Object data);

  Object visit(Reference node, Object data);

  Object visit(DestructuringAssignment node, Object data);

  Object visit(LetOrVar node, Object data);

  Object visit(Assignment node, Object data);

  Object visit(Return node, Object data);

  Object visit(Argument node, Object data);

  Object visit(AnonymousFunctionInvocation node, Object data);

  Object visit(FunctionInvocation node, Object data);

  Object visit(ConditionalBranching node, Object data);

  Object visit(Case node, Object data);

  Object visit(Match node, Object data);
}
