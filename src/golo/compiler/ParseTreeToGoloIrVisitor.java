package golo.compiler;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import golo.parser.*;
import golo.parser.ast.*;

import golo.lang.ir.Block;
import golo.lang.ir.CollectionLiteral;
import golo.lang.ir.ConditionalBranching;
import golo.lang.ir.DestructuringAssignment;
import golo.lang.ir.ExpressionStatement;
import golo.lang.ir.FunctionInvocation;
import golo.lang.ir.MacroInvocation;
import golo.lang.ir.MethodInvocation;
import golo.lang.ir.TryCatchFinally;
import golo.lang.ir.UnionValue;
import golo.lang.ir.*;

import static java.util.Collections.nCopies;
import static golo.compiler.GoloCompilationException.Problem.Type.*;
import static golo.lang.Messages.message;

public class ParseTreeToGoloIrVisitor implements GoloParserVisitor {

  public ParseTreeToGoloIrVisitor() {
  }

  @Override
  public Object visit(ParseError node, Object data) {
    return null;
  }

  private static final class Context {
    public GoloModule module;
    private final Deque<FunctionContainer> functionContainersStack = new LinkedList<>();
    private final Deque<Deque<Object>> objectStack = new LinkedList<>();
    private final Deque<MacroInvocation> macroInvocationStack = new LinkedList<>();
    private boolean mustAddFunction = true;
    private final Deque<ReferenceTable> referenceTableStack = new LinkedList<>();
    public boolean inLocalDeclaration = false;
    private GoloCompilationException.Builder exceptionBuilder;

    public void newObjectStack() {
      objectStack.push(new LinkedList<>());
    }

    public void popObjectStack() {
      objectStack.pop();
    }

    public void push(Object object) {
      if (objectStack.isEmpty()) {
        newObjectStack();
      }
      objectStack.peek().push(object);
    }

    public Object peek() {
      return objectStack.peek().peek();
    }

    public Object pop() {
      return objectStack.peek().pop();
    }

    public Block enterScope() {
      ReferenceTable blockReferenceTable = referenceTableStack.peek().fork();
      referenceTableStack.push(blockReferenceTable);
      return Block.empty().ref(blockReferenceTable);
    }

    public void leaveScope() {
      referenceTableStack.pop();
    }

    public void enterMacroInvocation(MacroInvocation macro) {
      macroInvocationStack.push(macro);
      mustAddFunction = false;
    }

    public void leaveMacroInvocation(boolean isTopLevel) {
      MacroInvocation mac = macroInvocationStack.pop();
      mustAddFunction = !inMacroInvocation();
      if (isTopLevel && mustAddFunction) {
        this.functionContainersStack.peek().addMacroInvocation(mac);
      } else {
        this.push(mac);
      }
    }

    public boolean inMacroInvocation() {
      return !macroInvocationStack.isEmpty();
    }

    public MacroInvocation currentMacroInvocation() {
      return macroInvocationStack.peek();
    }

    public GoloModule createModule(String name) {
      ReferenceTable global = new ReferenceTable();
      referenceTableStack.push(global);
      module = GoloModule.create(PackageAndClass.of(name), global);
      functionContainersStack.push(module);
      return module;
    }

    public void enterAugmentation(AugmentDeclaration node) {
      functionContainersStack.push(Augmentation.of(node.getTarget()).with(node.getAugmentationNames()).ofAST(node));
      mustAddFunction = true;
      newObjectStack();
    }

    public void leaveAugmentation() {
      popObjectStack();
      Augmentation converted = decoratorsAsMacroCalls((Augmentation) functionContainersStack.pop());
      if (converted == null) {
        return;
      }
      if (inMacroInvocation()) {
        push(converted);
      } else {
        module.add(converted);
      }
      mustAddFunction = !inMacroInvocation();
    }

    public void enterNamedAugmentation(NamedAugmentationDeclaration node) {
      NamedAugmentation namedAugmentation = NamedAugmentation.of(node.getName()).ofAST(node);
      functionContainersStack.push(namedAugmentation);
      mustAddFunction = true;
      newObjectStack();
    }

    public void leaveNamedAugmentation() {
      popObjectStack();
      NamedAugmentation converted = decoratorsAsMacroCalls((NamedAugmentation) functionContainersStack.pop());
      if (converted == null) {
        return;
      }
      if (inMacroInvocation()) {
        push(converted);
      } else {
        module.add(converted);
      }
      mustAddFunction = !inMacroInvocation();
    }

    public <T extends GoloElement<?>> MacroInvocation convertDecoratorsAsMacroCalls(T topLevel) {
      MacroInvocation decoratorLike = null;
      while (this.peek() instanceof Decorator) {
        decoratorLike = asMacroInvocation(((Decorator) this.pop()).expression())
            .withArgs(decoratorLike == null ? topLevel : decoratorLike);
      }
      return decoratorLike;
    }

    private static MacroInvocation asMacroInvocation(ExpressionStatement<?> expressionStatement) {
      if (expressionStatement instanceof ReferenceLookup) {
        return MacroInvocation.call(((ReferenceLookup) expressionStatement).getName())
            .positionInSourceCode(expressionStatement.positionInSourceCode());
      } else if (expressionStatement instanceof FunctionInvocation) {
        FunctionInvocation f = (FunctionInvocation) expressionStatement;
        return MacroInvocation.create(f.getName(), f.getArguments().toArray())
            .positionInSourceCode(expressionStatement.positionInSourceCode());
      }
      throw new IllegalArgumentException("Can't convert this decorator into a macro invocation");
    }

    private <T extends GoloElement<?>> T decoratorsAsMacroCalls(T topLevel) {
      MacroInvocation decoratorLike = convertDecoratorsAsMacroCalls(topLevel);
      if (decoratorLike != null) {
        this.module.addMacroInvocation(decoratorLike);
        return null;
      }
      return topLevel;
    }

    public <N extends NamedNode, T extends GoloElement<T>> void addType(N node, T type) {
      T converted = decoratorsAsMacroCalls(type);
      if (converted == null) {
        return;
      }
      if (inMacroInvocation()) {
        this.push(converted);
      } else if (!checkExistingSubtype(node, node.getName())) {
        module.add(converted);
      }
    }

    public void addImport(ModuleImport i) {
      if (inMacroInvocation()) {
        this.push(i);
      } else {
        this.module.add(i);
      }
    }

    public void addFunction(GoloFunction function) {
      if (!mustAddFunction) {
        this.push(function);
        return;
      }
      FunctionContainer container = this.functionContainersStack.peek();
      GoloFunction firstDeclaration = container.getFunction(function);
      if (firstDeclaration != null) {
        errorMessage(AMBIGUOUS_DECLARATION, function, message("ambiguous_function_declaration", function.getName(),
            firstDeclaration == null ? "unknown" : firstDeclaration.positionInSourceCode()));
      } else if (function.isInAugment() && function.getArity() == 0) {
        errorMessage(AUGMENT_FUNCTION_NO_ARGS, function,
            message("augment_function_no_args", function.getName(), container.getPackageAndClass()));
      } else {
        container.addFunction(function);
      }
    }

    public boolean checkExistingSubtype(SimpleNode node, String name) {
      GoloElement<?> existing = module.getSubtypeByName(name);
      if (existing != null) {
        errorMessage(AMBIGUOUS_DECLARATION, node,
            message("ambiguous_type_declaration", name, existing.positionInSourceCode()));
        return true;
      }
      return false;
    }

    public GoloFunction getOrCreateFunction() {
      if (!(peek() instanceof GoloFunction)) {
        push(GoloFunction.function(null).synthetic().local().asClosure());
      }
      return (GoloFunction) peek();
    }

    private LocalReference.Kind referenceKindOf(LetOrVar.Type type, boolean moduleState) {
      if (moduleState) {
        return type == LetOrVar.Type.LET ? LocalReference.Kind.MODULE_CONSTANT : LocalReference.Kind.MODULE_VARIABLE;
      } else {
        return type == LetOrVar.Type.LET ? LocalReference.Kind.CONSTANT : LocalReference.Kind.VARIABLE;
      }
    }

    public LocalReference getOrCreateReference(LetOrVar node) {
      return getOrCreateReference(node.getType(), node.getName(), node.isModuleState(), node);
    }

    public LocalReference getOrCreateReference(golo.parser.ast.DestructuringAssignment node, String name) {
      return getOrCreateReference(node.getType(), name, false, node);
    }

    public LocalReference getReference(String name, SimpleNode node) {
      if (inLocalDeclaration) {
        return getOrCreateReference(LetOrVar.Type.LET, name, false, node);
      }
      return referenceTableStack.peek().get(name);
    }

    private LocalReference getOrCreateReference(LetOrVar.Type type, String name, boolean module, SimpleNode node) {
      if (type != null) {
        LocalReference val = LocalReference.of(name).kind(referenceKindOf(type, module)).ofAST(node);
        if (!inLocalDeclaration) {
          referenceTableStack.peek().add(val);
        }
        return val;
      }
      return getReference(name, node);
    }

    public void setExceptionBuilder(GoloCompilationException.Builder builder) {
      exceptionBuilder = builder;
    }

    private GoloCompilationException.Builder getOrCreateExceptionBuilder() {
      if (exceptionBuilder == null) {
        exceptionBuilder = new GoloCompilationException.Builder(module.getPackageAndClass().toString());
      }
      return exceptionBuilder;
    }

    private String errorDescription(PositionInSourceCode position, String message) {
      return message + ' ' + message("source_position", position.getStartLine(), position.getStartColumn());
    }

    public void errorMessage(GoloCompilationException.Problem.Type type, SimpleNode node, String message) {
      getOrCreateExceptionBuilder().report(type, node, errorDescription(node.getPositionInSourceCode(), message));
    }

    public void errorMessage(GoloCompilationException.Problem.Type type, GoloElement<?> node, String message) {
      getOrCreateExceptionBuilder().report(type, node, errorDescription(node.positionInSourceCode(), message));
    }
  }

  public GoloModule transform(CompilationUnit compilationUnit, GoloCompilationException.Builder builder) {
    Context context = new Context();
    context.newObjectStack();
    context.setExceptionBuilder(builder);
    visit(compilationUnit, context);
    return context.module.sourceFile(compilationUnit.getFilename());
  }

  @Override
  public Object visit(SimpleNode node, Object data) {
    throw new IllegalStateException("visit(SimpleNode) shall never be invoked: " + node.getClass());
  }

  @Override
  public Object visit(CompilationUnit node, Object data) {
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ModuleDeclaration node, Object data) {
    Context context = (Context) data;
    GoloModule module = context.createModule(node.getName()).ofAST(node);
    node.childrenAccept(this, data);
    module.decoratorMacro(context.convertDecoratorsAsMacroCalls(module));
    return data;
  }

  @Override
  public Object visit(ImportDeclaration node, Object data) {
    Context context = (Context) data;
    PackageAndClass name;
    if (node.isRelative()) {
      name = context.module.getPackageAndClass().createSiblingClass(node.getName());
    } else {
      name = PackageAndClass.of(node.getName());
    }
    if (node.getMultiple().isEmpty()) {
      context.addImport(ModuleImport.of(name).ofAST(node));
    } else {
      for (String sub : node.getMultiple()) {
        context.addImport(ModuleImport.of(name.createSubPackage(sub)).ofAST(node));
      }
    }
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(ToplevelDeclaration node, Object data) {
    return node.childrenAccept(this, data);
  }

  @Override
  public Object visit(MemberDeclaration node, Object data) {
    Context context = (Context) data;
    context.push(Member.of(node.getName()).ofAST(node));
    return context;
  }

  @Override
  public Object visit(StructDeclaration node, Object data) {
    Context context = (Context) data;
    Struct theStruct = Struct.struct(node.getName()).ofAST(node);
    for (int i = 0; i < node.getNumChildren(); i++) {
      node.getChild(i).accept(this, context);
      theStruct.withMember(context.pop());
    }
    context.addType(node, theStruct);
    return data;
  }

  @Override
  public Object visit(UnionDeclaration node, Object data) {
    Context context = (Context) data;
    context.push(Union.union(node.getName()).ofAST(node));
    node.childrenAccept(this, data);
    context.addType(node, (Union) context.pop());
    return data;
  }

  @Override
  public Object visit(golo.parser.ast.UnionValue node, Object data) {
    Context context = (Context) data;
    UnionValue value = new UnionValue(node.getName()).ofAST(node);
    for (int i = 0; i < node.getNumChildren(); i++) {
      node.getChild(i).accept(this, context);
      if (context.peek() instanceof Member) {
        value.withMember(context.pop());
      }
    }
    MacroInvocation decoLike = context.convertDecoratorsAsMacroCalls(value);
    Union currentUnion = (Union) context.peek();
    if (decoLike != null) {
      currentUnion.addMacroInvocation(decoLike);
    } else if (!currentUnion.addValue(value)) {
      context.errorMessage(AMBIGUOUS_DECLARATION, node, message("ambiguous_unionvalue_declaration", node.getName()));
    }
    return data;
  }

  @Override
  public Object visit(AugmentDeclaration node, Object data) {
    Context context = (Context) data;
    context.enterAugmentation(node);
    node.childrenAccept(this, data);
    context.leaveAugmentation();
    return data;
  }

  @Override
  public Object visit(DecoratorDeclaration node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    context.push(Decorator.of(context.pop()).constant(node.isConstant()).ofAST(node));
    return data;
  }

  @Override
  public Object visit(NamedAugmentationDeclaration node, Object data) {
    Context context = (Context) data;
    context.enterNamedAugmentation(node);
    node.childrenAccept(this, data);
    context.leaveNamedAugmentation();
    return data;
  }

  @Override
  public Object visit(FunctionDeclaration node, Object data) {
    Context context = (Context) data;
    GoloFunction function = GoloFunction.function(node.getName()).ofAST(node).local(node.isLocal())
        .inAugment(node.isAugmentation()).decorator(node.isDecorator()).asMacro(node.isMacro());
    while (context.peek() instanceof Decorator) {
      function.decoratedWith(context.pop());
    }
    context.push(function);
    node.childrenAccept(this, data);
    context.pop();
    return data;
  }

  @Override
  public Object visit(Continue node, Object data) {
    Context context = (Context) data;
    LoopBreakFlowStatement statement = LoopBreakFlowStatement.newContinue().ofAST(node);
    context.push(statement);
    return data;
  }

  @Override
  public Object visit(Break node, Object data) {
    Context context = (Context) data;
    LoopBreakFlowStatement statement = LoopBreakFlowStatement.newBreak().ofAST(node);
    context.push(statement);
    return data;
  }

  @Override
  public Object visit(Function node, Object data) {
    Context context = (Context) data;
    GoloFunction function = context.getOrCreateFunction().ofAST(node).varargs(node.isVarargs())
        .withParameters(node.getParameters());

    if (node.isCompactForm()) {
      // TODO: refactor
      SimpleNode astChild = node.getChild(0);
      Return astReturn = new Return(0);
      astReturn.addChild(astChild, 0);
      var astBlock = new golo.parser.ast.Block(0);
      astBlock.addChild(astReturn, 0);
      astBlock.accept(this, data);
      // FIXME ?
      // if (function.isSynthetic()) {
      //   context.pop();
      // }
      // node.getChild(0).accept(this, data);
      // function.block(returns(context.pop()));
      // context.push(function.getBlock());
    } else {
      node.childrenAccept(this, data);
    }
    if (function.isSynthetic()) {
      context.pop();
      context.push(function.asClosureReference());
    } else {
      context.addFunction(function);
      context.pop();
    }
    return data;
  }

  @Override
  public Object visit(UnaryExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    context.push(UnaryOperation.create(node.getOperator(), context.pop()).ofAST(node));
    return data;
  }

  @Override
  public Object visit(Literal node, Object data) {
    Context context = (Context) data;
    ConstantStatement constantStatement = ConstantStatement.of(node.getLiteralValue()).ofAST(node);
    context.push(constantStatement);
    return data;
  }

  @Override
  public Object visit(golo.parser.ast.CollectionLiteral node, Object data) {
    if (node.isComprehension()) {
      return createCollectionComprehension(node, (Context) data);
    }
    return createCollectionLiteral(node, (Context) data);
  }

  private Object createCollectionLiteral(golo.parser.ast.CollectionLiteral node, Context context) {
    CollectionLiteral collection = CollectionLiteral.create(node.getType()).ofAST(node);
    for (int i = 0; i < node.getNumChildren(); i++) {
      node.getChild(i).accept(this, context);
      collection.add(context.pop());
    }
    context.push(collection);
    return context;
  }

  private Object createCollectionComprehension(golo.parser.ast.CollectionLiteral node, Context context) {
    CollectionComprehension col = CollectionComprehension.of(node.getType()).ofAST(node);
    node.getChild(0).accept(this, context);
    col.expression(context.pop());
    for (int i = 1; i < node.getNumChildren(); i++) {
      node.getChild(i).accept(this, context);
      col.loop(((Block) context.pop()).getStatements().get(0));
    }
    context.push(col);
    return context;
  }

  @Override
  public Object visit(Reference node, Object data) {
    ((Context) data).push(ReferenceLookup.of(node.getName()).ofAST(node));
    return data;
  }

  @Override
  public Object visit(LetOrVar node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    AssignmentStatement assignmentStatement = AssignmentStatement
        .create(context.getOrCreateReference(node), context.pop(), true).ofAST(node);
    if (node.isModuleState()) {
      context.module.add(assignmentStatement);
    } else {
      context.push(assignmentStatement);
    }
    return data;
  }

  @Override
  public Object visit(Assignment node, Object data) {
    Context context = (Context) data;
    LocalReference reference = context.getReference(node.getName(), node);
    node.childrenAccept(this, data);
    if (reference == null) {
      context.errorMessage(UNDECLARED_REFERENCE, node, message("undeclared_reference_assignment", node.getName()));
    } else {
      context.push(AssignmentStatement.create(reference, context.pop(), false).ofAST(node));
    }
    return data;
  }

  @Override
  public Object visit(golo.parser.ast.DestructuringAssignment node, Object data) {
    Context context = (Context) data;
    node.getChild(0).accept(this, data);

    var builder = golo.lang.ir.DestructuringAssignment.destruct(context.pop()).ofAST(node)
        .declaring(node.getType() != null).varargs(node.isVarargs());

    for (String name : node.getNames()) {
      LocalReference val = context.getOrCreateReference(node, name);
      if (val != null) {
        builder.to(val);
      }
    }
    context.push(builder);
    return data;
  }

  @Override
  public Object visit(Return node, Object data) {
    Context context = (Context) data;
    if (node.getNumChildren() > 0) {
      node.childrenAccept(this, data);
    } else {
      context.push(ConstantStatement.of(null));
    }
    context.push(ReturnStatement.of(context.pop()).ofAST(node));
    return data;
  }

  @Override
  public Object visit(Argument node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    context.push(node.isNamed() ? NamedArgument.of(node.getName(), context.pop()) : context.pop());
    return data;
  }

  @Override
  public Object visit(Throw node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    context.push(ThrowStatement.of(context.pop()).ofAST(node));
    return data;
  }

  @Override
  public Object visit(golo.parser.ast.Block node, Object data) {
    Context context = (Context) data;
    Block block = context.enterScope().ofAST(node);
    if (context.peek() instanceof GoloFunction) {
      GoloFunction function = (GoloFunction) context.peek();
      function.block(block);
      if (function.isSynthetic()) {
        context.pop();
      }
    }
    context.push(block);
    for (int i = 0; i < node.getNumChildren(); i++) {
      SimpleNode child = (SimpleNode) node.getChild(i);
      child.accept(this, data);
      GoloStatement<?> statement = (GoloStatement) context.pop();
      block.add(statement);
    }
    context.leaveScope();
    return data;
  }

  @Override
  public Object visit(golo.parser.ast.FunctionInvocation node, Object data) {
    Context context = (Context) data;
    context
        .push(visitAbstractInvocation(data, node, FunctionInvocation.of(node.getName()).constant(node.isConstant())));
    return data;
  }

  @Override
  public Object visit(golo.parser.ast.MethodInvocation node, Object data) {
    Context context = (Context) data;
    context.push(visitAbstractInvocation(data, node, MethodInvocation.invoke(node.getName())));
    return data;
  }

  @Override
  public Object visit(AnonymousFunctionInvocation node, Object data) {
    Context context = (Context) data;
    ExpressionStatement<?> result = visitAbstractInvocation(data, node,
        FunctionInvocation.of(null).constant(node.isConstant()));
    if (node.isOnExpression()) {
      context.push(ExpressionStatement.of(context.pop()).call(result));
    } else {
      context.push(result);
    }
    return data;
  }

  private void checkNamedArgument(Context context, SimpleNode node, AbstractInvocation<?> invocation,
      ExpressionStatement<?> statement) {
    if (statement instanceof NamedArgument) {
      if (!invocation.namedArgumentsComplete()) {
        context.errorMessage(INCOMPLETE_NAMED_ARGUMENTS_USAGE, node,
            message("incomplete_named_arguments_usage", invocation.getClass().getName(), invocation.getName()));
      }
      invocation.withNamedArguments();
    }
  }

  @Override
  public Object visit(golo.parser.ast.MacroInvocation node, Object data) {
    Context context = (Context) data;
    context.enterMacroInvocation(MacroInvocation.call(node.getName()).ofAST(node));
    final int numChildren = node.getNumChildren();
    for (int i = 0; i < numChildren; i++) {
      SimpleNode argumentNode = (SimpleNode) node.getChild(i);
      argumentNode.accept(this, data);
      context.currentMacroInvocation().withArgs(context.pop());
    }
    context.leaveMacroInvocation(node.isTopLevel());
    return data;
  }

  private ExpressionStatement<?> visitAbstractInvocation(Object data, SimpleNode node,
      AbstractInvocation<?> invocation) {
    Context context = (Context) data;
    invocation.ofAST(node);
    int i = 0;
    final int numChildren = node.getNumChildren();
    for (i = 0; i < numChildren; i++) {
      SimpleNode argumentNode = (SimpleNode) node.getChild(i);
      if (argumentNode instanceof AnonymousFunctionInvocation) {
        break;
      }
      argumentNode.accept(this, context);
      ExpressionStatement<?> statement = ExpressionStatement.of(context.pop());
      checkNamedArgument(context, node, invocation, statement);
      invocation.withArgs(statement);
    }
    ExpressionStatement<?> result = invocation;
    if (i < numChildren) {
      for (; i < numChildren; i++) {
        node.getChild(i).accept(this, context);
        result = result.call(context.pop());
      }
    }
    return result;
  }

  @Override
  public Object visit(golo.parser.ast.ConditionalBranching node, Object data) {
    Context context = (Context) data;
    node.getChild(1).accept(this, data);
    node.getChild(0).accept(this, data);

    ConditionalBranching conditionalBranching = ConditionalBranching.branch().ofAST(node).condition(context.pop())
        .whenTrue(context.pop());

    if (node.getNumChildren() > 2) {
      node.getChild(2).accept(this, data);
      conditionalBranching.otherwise(context.pop());
    }
    context.push(conditionalBranching);
    return data;
  }

  private Object visitAlternatives(Object data, SimpleNode node, Alternatives<?> alternatives) {
    Context context = (Context) data;
    final int lastWhen = node.getNumChildren() - 1;
    for (int i = 0; i < lastWhen; i += 2) {
      node.getChild(i).accept(this, data);
      alternatives.when(context.pop());
      node.getChild(i + 1).accept(this, data);
      alternatives.then(context.pop());
    }
    node.getChild(lastWhen).accept(this, data);
    alternatives.otherwise(context.pop());
    context.push(alternatives);
    return data;
  }

  @Override
  public Object visit(Case node, Object data) {
    return visitAlternatives(data, node, CaseStatement.cases().ofAST(node));
  }

  @Override
  public Object visit(Match node, Object data) {
    return visitAlternatives(data, node, MatchExpression.match().ofAST(node));
  }

  @Override
  public Object visit(WhileLoop node, Object data) {
    Context context = (Context) data;
    node.getChild(1).accept(this, data);
    node.getChild(0).accept(this, data);
    context.push(LoopStatement.loop().condition(context.pop()).ofAST(node).block(Block.of(context.pop())));
    return data;
  }

  @Override
  public Object visit(ForLoop node, Object data) {
    Context context = (Context) data;
    Block containingBlock = context.enterScope();

    node.getChild(0).accept(this, data);
    node.getChild(1).accept(this, data);
    node.getChild(2).accept(this, data);

    LoopStatement loopStatement = LoopStatement.loop().ofAST(node).post(context.pop()).condition(context.pop())
        .init(context.pop());

    if (node.getNumChildren() == 4) {
      node.getChild(3).accept(this, data);
      loopStatement.block(Block.of(context.pop()));
    }
    context.push(containingBlock.add(loopStatement));
    context.leaveScope();
    return data;
  }

  @Override
  public Object visit(ForEachLoop node, Object data) {
    Context context = (Context) data;
    Block containingBlock = context.enterScope();

    node.getChild(0).accept(this, data);

    ForEachLoopStatement foreach = ForEachLoopStatement.create().ofAST(node).varargs(node.isVarargs())
        .in(context.pop());

    if (node.getElementIdentifier() != null) {
      foreach.var(node.getElementIdentifier());
    } else {
      for (String name : node.getNames()) {
        foreach.var(name);
      }
    }

    // there may be no block if we are in a collection comprehension, checking what we have...
    int numChildren = node.getNumChildren();
    if (numChildren > 2) {
      // when and block: it's a regular loop with a when clause
      node.getChild(2).accept(this, data);
      node.getChild(1).accept(this, data);
      foreach.when(context.pop()).block(context.pop());
    } else if (numChildren == 2) {
      // either a when and no block in collection comprehension or no when an block in regular loop
      node.getChild(1).accept(this, data);
      Object child = context.pop();
      if (child instanceof Block) {
        foreach.block(child);
      } else if (child instanceof ExpressionStatement) {
        foreach.when(child);
      } else {
        context.errorMessage(PARSING, node, message("syntax_foreach"));
      }
    }
    context.push(containingBlock.add(foreach));
    context.leaveScope();
    return data;
  }

  @Override
  public Object visit(golo.parser.ast.TryCatchFinally node, Object data) {
    Context context = (Context) data;
    boolean hasCatchBlock = (node.getExceptionId() != null);
    TryCatchFinally tryCatchFinally = TryCatchFinally.tryCatch().ofAST(node);

    context.enterScope();
    node.getChild(0).accept(this, data);
    tryCatchFinally.trying(context.pop());
    context.leaveScope();

    context.enterScope();
    node.getChild(1).accept(this, data);
    if (hasCatchBlock) {
      tryCatchFinally.catching(node.getExceptionId(), context.pop());
    } else {
      tryCatchFinally.finalizing(context.pop());
    }
    context.leaveScope();

    if (hasCatchBlock && node.getNumChildren() > 2) {
      context.enterScope();
      node.getChild(2).accept(this, data);
      tryCatchFinally.finalizing(context.pop());
      context.leaveScope();
    }

    context.push(tryCatchFinally);
    return data;
  }

  @Override
  public Object visit(golo.parser.ast.ExpressionStatement node, Object data) {
    node.childrenAccept(this, data);
    return data;
  }

  private void createOperatorChain(List<String> opSymbols, SimpleNode node, Context context) {
    List<OperatorType> operators = opSymbols.stream().map(OperatorType::of).collect(Collectors.toList());
    List<ExpressionStatement<?>> statements = operatorStatements(context, operators.size());
    ExpressionStatement<?> operation = assembleBinaryOperation(statements, operators).ofAST(node);
    context.push(operation);
  }

  @Override
  public Object visit(InvocationExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    createOperatorChain(node.getOperators(), node, context);
    return data;
  }

  private BinaryOperation assembleBinaryOperation(List<ExpressionStatement<?>> statements,
      List<OperatorType> operators) {
    BinaryOperation current = null;
    int i = 2;
    for (OperatorType operator : operators) {
      if (current == null) {
        current = BinaryOperation.create(operator, statements.get(0), statements.get(1));
      } else {
        current = BinaryOperation.create(operator, current, statements.get(i));
        i++;
      }
    }
    return current;
  }

  private List<ExpressionStatement<?>> operatorStatements(Context context, int operatorsCount) {
    LinkedList<ExpressionStatement<?>> statements = new LinkedList<>();
    for (int i = 0; i < operatorsCount + 1; i++) {
      statements.addFirst(ExpressionStatement.of(context.pop()));
    }
    return statements;
  }

  @Override
  public Object visit(MultiplicativeExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    createOperatorChain(node.getOperators(), node, context);
    return data;
  }

  @Override
  public Object visit(AdditiveExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    createOperatorChain(node.getOperators(), node, context);
    return data;
  }

  @Override
  public Object visit(RelationalExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    BinaryOperation operation = BinaryOperation.of(node.getOperator()).right(context.pop()).left(context.pop())
        .ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(EqualityExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, data);
    BinaryOperation operation = BinaryOperation.of(node.getOperator()).right(context.pop()).left(context.pop())
        .ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(AndExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    List<ExpressionStatement<?>> statements = operatorStatements(context, node.count());
    BinaryOperation operation = assembleBinaryOperation(statements, nCopies(node.count(), OperatorType.AND))
        .ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(OrExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    List<ExpressionStatement<?>> statements = operatorStatements(context, node.count());
    BinaryOperation operation = assembleBinaryOperation(statements, nCopies(node.count(), OperatorType.OR)).ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(OrIfNullExpression node, Object data) {
    Context context = (Context) data;
    node.childrenAccept(this, context);
    List<ExpressionStatement<?>> statements = operatorStatements(context, node.count());
    BinaryOperation operation = assembleBinaryOperation(statements, nCopies(node.count(), OperatorType.ORIFNULL))
        .ofAST(node);
    context.push(operation);
    return data;
  }

  @Override
  public Object visit(LocalDeclaration node, Object data) {
    Context context = (Context) data;
    ExpressionStatement<?> expr = (ExpressionStatement<?>) context.peek();
    boolean oldState = context.inLocalDeclaration;
    context.inLocalDeclaration = true;
    for (int i = 0; i < node.getNumChildren(); i++) {
      node.getChild(i).accept(this, data);
      try {
        expr.with(context.pop());
      } catch (UnsupportedOperationException ex) {
        context.errorMessage(PARSING, node, ex.getMessage());
      }
    }
    context.inLocalDeclaration = oldState;
    return data;
  }
}
