// Generated from Java8CommentSupported.g4 by ANTLR 4.2
package parser.grammar;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link Java8CommentSupportedParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface Java8CommentSupportedVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classMemberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassMemberDeclaration(@NotNull Java8CommentSupportedParser.ClassMemberDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#statementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementNoShortIf(@NotNull Java8CommentSupportedParser.StatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#annotationTypeElementDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationTypeElementDeclaration(@NotNull Java8CommentSupportedParser.AnnotationTypeElementDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodInvocation_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodInvocation_lf_primary(@NotNull Java8CommentSupportedParser.MethodInvocation_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#annotationTypeBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationTypeBody(@NotNull Java8CommentSupportedParser.AnnotationTypeBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#constantModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantModifier(@NotNull Java8CommentSupportedParser.ConstantModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#lambdaBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaBody(@NotNull Java8CommentSupportedParser.LambdaBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(@NotNull Java8CommentSupportedParser.ArgumentListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBodyDeclaration(@NotNull Java8CommentSupportedParser.ClassBodyDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classInstanceCreationExpression_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassInstanceCreationExpression_lfno_primary(@NotNull Java8CommentSupportedParser.ClassInstanceCreationExpression_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpression(@NotNull Java8CommentSupportedParser.UnaryExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classType_lfno_classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassType_lfno_classOrInterfaceType(@NotNull Java8CommentSupportedParser.ClassType_lfno_classOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#arrayType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayType(@NotNull Java8CommentSupportedParser.ArrayTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#simpleTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleTypeName(@NotNull Java8CommentSupportedParser.SimpleTypeNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#expressionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionName(@NotNull Java8CommentSupportedParser.ExpressionNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#statementWithoutTrailingSubstatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementWithoutTrailingSubstatement(@NotNull Java8CommentSupportedParser.StatementWithoutTrailingSubstatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#constructorDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorDeclarator(@NotNull Java8CommentSupportedParser.ConstructorDeclaratorContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#assertStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssertStatement(@NotNull Java8CommentSupportedParser.AssertStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#arrayCreationExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayCreationExpression(@NotNull Java8CommentSupportedParser.ArrayCreationExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannArrayType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannArrayType(@NotNull Java8CommentSupportedParser.UnannArrayTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#variableDeclaratorId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaratorId(@NotNull Java8CommentSupportedParser.VariableDeclaratorIdContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#tryWithResourcesStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTryWithResourcesStatement(@NotNull Java8CommentSupportedParser.TryWithResourcesStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArguments(@NotNull Java8CommentSupportedParser.TypeArgumentsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#exceptionType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionType(@NotNull Java8CommentSupportedParser.ExceptionTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#exceptionTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionTypeList(@NotNull Java8CommentSupportedParser.ExceptionTypeListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#additiveExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpression(@NotNull Java8CommentSupportedParser.AdditiveExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#relationalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpression(@NotNull Java8CommentSupportedParser.RelationalExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#referenceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceType(@NotNull Java8CommentSupportedParser.ReferenceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayAccess_lf_primary(@NotNull Java8CommentSupportedParser.ArrayAccess_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#inferredFormalParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInferredFormalParameterList(@NotNull Java8CommentSupportedParser.InferredFormalParameterListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(@NotNull Java8CommentSupportedParser.ReturnStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeImportOnDemandDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeImportOnDemandDeclaration(@NotNull Java8CommentSupportedParser.TypeImportOnDemandDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameters(@NotNull Java8CommentSupportedParser.TypeParametersContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#lastFormalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLastFormalParameter(@NotNull Java8CommentSupportedParser.LastFormalParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(@NotNull Java8CommentSupportedParser.LiteralContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#result}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResult(@NotNull Java8CommentSupportedParser.ResultContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#fieldAccess_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAccess_lfno_primary(@NotNull Java8CommentSupportedParser.FieldAccess_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#variableDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclarator(@NotNull Java8CommentSupportedParser.VariableDeclaratorContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#finally_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFinally_(@NotNull Java8CommentSupportedParser.Finally_Context ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBody(@NotNull Java8CommentSupportedParser.ClassBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannInterfaceType_lfno_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannInterfaceType_lfno_unannClassOrInterfaceType(@NotNull Java8CommentSupportedParser.UnannInterfaceType_lfno_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#enumDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumDeclaration(@NotNull Java8CommentSupportedParser.EnumDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#constantDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantDeclaration(@NotNull Java8CommentSupportedParser.ConstantDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixExpression(@NotNull Java8CommentSupportedParser.PostfixExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotation(@NotNull Java8CommentSupportedParser.AnnotationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#variableInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableInitializer(@NotNull Java8CommentSupportedParser.VariableInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#staticImportOnDemandDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStaticImportOnDemandDeclaration(@NotNull Java8CommentSupportedParser.StaticImportOnDemandDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(@NotNull Java8CommentSupportedParser.ExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#throwStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThrowStatement(@NotNull Java8CommentSupportedParser.ThrowStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodInvocation(@NotNull Java8CommentSupportedParser.MethodInvocationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#singleStaticImportDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleStaticImportDeclaration(@NotNull Java8CommentSupportedParser.SingleStaticImportDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#lambdaParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaParameters(@NotNull Java8CommentSupportedParser.LambdaParametersContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#conditionalAndExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalAndExpression(@NotNull Java8CommentSupportedParser.ConditionalAndExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpression(@NotNull Java8CommentSupportedParser.MultiplicativeExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#packageModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageModifier(@NotNull Java8CommentSupportedParser.PackageModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorDeclaration(@NotNull Java8CommentSupportedParser.ConstructorDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#primaryNoNewArray_lfno_arrayAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lfno_arrayAccess(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lfno_arrayAccessContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannTypeVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannTypeVariable(@NotNull Java8CommentSupportedParser.UnannTypeVariableContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#normalInterfaceDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNormalInterfaceDeclaration(@NotNull Java8CommentSupportedParser.NormalInterfaceDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#interfaceType_lfno_classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceType_lfno_classOrInterfaceType(@NotNull Java8CommentSupportedParser.InterfaceType_lfno_classOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#constructorModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorModifier(@NotNull Java8CommentSupportedParser.ConstructorModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#enumConstantName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstantName(@NotNull Java8CommentSupportedParser.EnumConstantNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classInstanceCreationExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassInstanceCreationExpression(@NotNull Java8CommentSupportedParser.ClassInstanceCreationExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDeclarator(@NotNull Java8CommentSupportedParser.MethodDeclaratorContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#annotationTypeMemberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationTypeMemberDeclaration(@NotNull Java8CommentSupportedParser.AnnotationTypeMemberDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#preDecrementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreDecrementExpression(@NotNull Java8CommentSupportedParser.PreDecrementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#variableInitializerList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableInitializerList(@NotNull Java8CommentSupportedParser.VariableInitializerListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#extendsInterfaces}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtendsInterfaces(@NotNull Java8CommentSupportedParser.ExtendsInterfacesContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#elementValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValue(@NotNull Java8CommentSupportedParser.ElementValueContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#arrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayInitializer(@NotNull Java8CommentSupportedParser.ArrayInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#arrayAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayAccess(@NotNull Java8CommentSupportedParser.ArrayAccessContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodModifier(@NotNull Java8CommentSupportedParser.MethodModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannClassType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannClassType(@NotNull Java8CommentSupportedParser.UnannClassTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#lambdaExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaExpression(@NotNull Java8CommentSupportedParser.LambdaExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentExpression(@NotNull Java8CommentSupportedParser.AssignmentExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameterList(@NotNull Java8CommentSupportedParser.TypeParameterListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#normalClassDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNormalClassDeclaration(@NotNull Java8CommentSupportedParser.NormalClassDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#formalParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameterList(@NotNull Java8CommentSupportedParser.FormalParameterListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#enhancedForStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnhancedForStatementNoShortIf(@NotNull Java8CommentSupportedParser.EnhancedForStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#annotationTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationTypeDeclaration(@NotNull Java8CommentSupportedParser.AnnotationTypeDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(@NotNull Java8CommentSupportedParser.CompilationUnitContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#wildcardBounds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWildcardBounds(@NotNull Java8CommentSupportedParser.WildcardBoundsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#primaryNoNewArray_lf_arrayAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lf_arrayAccess(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lf_arrayAccessContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#enhancedForStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnhancedForStatement(@NotNull Java8CommentSupportedParser.EnhancedForStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#switchBlockStatementGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchBlockStatementGroup(@NotNull Java8CommentSupportedParser.SwitchBlockStatementGroupContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeVariable(@NotNull Java8CommentSupportedParser.TypeVariableContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameter(@NotNull Java8CommentSupportedParser.TypeParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDeclaration(@NotNull Java8CommentSupportedParser.MethodDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#interfaceBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceBody(@NotNull Java8CommentSupportedParser.InterfaceBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodBody(@NotNull Java8CommentSupportedParser.MethodBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#dims}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDims(@NotNull Java8CommentSupportedParser.DimsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArgument(@NotNull Java8CommentSupportedParser.TypeArgumentContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannPrimitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannPrimitiveType(@NotNull Java8CommentSupportedParser.UnannPrimitiveTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#explicitConstructorInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitConstructorInvocation(@NotNull Java8CommentSupportedParser.ExplicitConstructorInvocationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#enumBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumBody(@NotNull Java8CommentSupportedParser.EnumBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#additionalBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditionalBound(@NotNull Java8CommentSupportedParser.AdditionalBoundContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#primaryNoNewArray_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lfno_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannClassOrInterfaceType(@NotNull Java8CommentSupportedParser.UnannClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#ifThenStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfThenStatement(@NotNull Java8CommentSupportedParser.IfThenStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#postDecrementExpression_lf_postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostDecrementExpression_lf_postfixExpression(@NotNull Java8CommentSupportedParser.PostDecrementExpression_lf_postfixExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#interfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceType(@NotNull Java8CommentSupportedParser.InterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodReference_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodReference_lf_primary(@NotNull Java8CommentSupportedParser.MethodReference_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#andExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(@NotNull Java8CommentSupportedParser.AndExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#enumConstantModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstantModifier(@NotNull Java8CommentSupportedParser.EnumConstantModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannClassType_lfno_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannClassType_lfno_unannClassOrInterfaceType(@NotNull Java8CommentSupportedParser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(@NotNull Java8CommentSupportedParser.StatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#fieldModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldModifier(@NotNull Java8CommentSupportedParser.FieldModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#markerAnnotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMarkerAnnotation(@NotNull Java8CommentSupportedParser.MarkerAnnotationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#resourceList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceList(@NotNull Java8CommentSupportedParser.ResourceListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayAccess_lfno_primary(@NotNull Java8CommentSupportedParser.ArrayAccess_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#staticInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStaticInitializer(@NotNull Java8CommentSupportedParser.StaticInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#conditionalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalExpression(@NotNull Java8CommentSupportedParser.ConditionalExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDeclaration(@NotNull Java8CommentSupportedParser.FieldDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#leftHandSide}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeftHandSide(@NotNull Java8CommentSupportedParser.LeftHandSideContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#basicForStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasicForStatement(@NotNull Java8CommentSupportedParser.BasicForStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(@NotNull Java8CommentSupportedParser.WhileStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#packageDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageDeclaration(@NotNull Java8CommentSupportedParser.PackageDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#localVariableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocalVariableDeclaration(@NotNull Java8CommentSupportedParser.LocalVariableDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#superinterfaces}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuperinterfaces(@NotNull Java8CommentSupportedParser.SuperinterfacesContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#interfaceMemberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMemberDeclaration(@NotNull Java8CommentSupportedParser.InterfaceMemberDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classInstanceCreationExpression_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassInstanceCreationExpression_lf_primary(@NotNull Java8CommentSupportedParser.ClassInstanceCreationExpression_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#switchBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchBlock(@NotNull Java8CommentSupportedParser.SwitchBlockContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#forInit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForInit(@NotNull Java8CommentSupportedParser.ForInitContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#blockStatements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStatements(@NotNull Java8CommentSupportedParser.BlockStatementsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#formalParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameters(@NotNull Java8CommentSupportedParser.FormalParametersContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(@NotNull Java8CommentSupportedParser.CommentContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArgumentsOrDiamond(@NotNull Java8CommentSupportedParser.TypeArgumentsOrDiamondContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#defaultValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultValue(@NotNull Java8CommentSupportedParser.DefaultValueContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(@NotNull Java8CommentSupportedParser.TypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#elementValuePairList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValuePairList(@NotNull Java8CommentSupportedParser.ElementValuePairListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#synchronizedStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSynchronizedStatement(@NotNull Java8CommentSupportedParser.SynchronizedStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannClassType_lf_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannClassType_lf_unannClassOrInterfaceType(@NotNull Java8CommentSupportedParser.UnannClassType_lf_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#superclass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuperclass(@NotNull Java8CommentSupportedParser.SuperclassContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(@NotNull Java8CommentSupportedParser.BlockContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#expressionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStatement(@NotNull Java8CommentSupportedParser.ExpressionStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#preIncrementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreIncrementExpression(@NotNull Java8CommentSupportedParser.PreIncrementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#enumBodyDeclarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumBodyDeclarations(@NotNull Java8CommentSupportedParser.EnumBodyDeclarationsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#dimExprs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimExprs(@NotNull Java8CommentSupportedParser.DimExprsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#forUpdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForUpdate(@NotNull Java8CommentSupportedParser.ForUpdateContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#emptyStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyStatement(@NotNull Java8CommentSupportedParser.EmptyStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#primaryNoNewArray_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lf_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#annotationTypeElementModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationTypeElementModifier(@NotNull Java8CommentSupportedParser.AnnotationTypeElementModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#shiftExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftExpression(@NotNull Java8CommentSupportedParser.ShiftExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#fieldAccess_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAccess_lf_primary(@NotNull Java8CommentSupportedParser.FieldAccess_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#instanceInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceInitializer(@NotNull Java8CommentSupportedParser.InstanceInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannType(@NotNull Java8CommentSupportedParser.UnannTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#integralType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegralType(@NotNull Java8CommentSupportedParser.IntegralTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#postIncrementExpression_lf_postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostIncrementExpression_lf_postfixExpression(@NotNull Java8CommentSupportedParser.PostIncrementExpression_lf_postfixExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassOrInterfaceType(@NotNull Java8CommentSupportedParser.ClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#equalityExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpression(@NotNull Java8CommentSupportedParser.EqualityExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#normalAnnotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNormalAnnotation(@NotNull Java8CommentSupportedParser.NormalAnnotationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeBound(@NotNull Java8CommentSupportedParser.TypeBoundContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(@NotNull Java8CommentSupportedParser.PrimaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassModifier(@NotNull Java8CommentSupportedParser.ClassModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#fieldAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAccess(@NotNull Java8CommentSupportedParser.FieldAccessContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(@NotNull Java8CommentSupportedParser.TypeNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#constructorBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorBody(@NotNull Java8CommentSupportedParser.ConstructorBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#catchClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchClause(@NotNull Java8CommentSupportedParser.CatchClauseContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#labeledStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabeledStatement(@NotNull Java8CommentSupportedParser.LabeledStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#switchLabels}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchLabels(@NotNull Java8CommentSupportedParser.SwitchLabelsContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#formalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameter(@NotNull Java8CommentSupportedParser.FormalParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#interfaceType_lf_classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceType_lf_classOrInterfaceType(@NotNull Java8CommentSupportedParser.InterfaceType_lf_classOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#singleTypeImportDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleTypeImportDeclaration(@NotNull Java8CommentSupportedParser.SingleTypeImportDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#singleElementAnnotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleElementAnnotation(@NotNull Java8CommentSupportedParser.SingleElementAnnotationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValueArrayInitializer(@NotNull Java8CommentSupportedParser.ElementValueArrayInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#constantExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantExpression(@NotNull Java8CommentSupportedParser.ConstantExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(@NotNull Java8CommentSupportedParser.ForStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeArgumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArgumentList(@NotNull Java8CommentSupportedParser.TypeArgumentListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#postDecrementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostDecrementExpression(@NotNull Java8CommentSupportedParser.PostDecrementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#receiverParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReceiverParameter(@NotNull Java8CommentSupportedParser.ReceiverParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#whileStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatementNoShortIf(@NotNull Java8CommentSupportedParser.WhileStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#enumConstantList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstantList(@NotNull Java8CommentSupportedParser.EnumConstantListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#doStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoStatement(@NotNull Java8CommentSupportedParser.DoStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#catchType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchType(@NotNull Java8CommentSupportedParser.CatchTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#forStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatementNoShortIf(@NotNull Java8CommentSupportedParser.ForStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#ifThenElseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfThenElseStatement(@NotNull Java8CommentSupportedParser.IfThenElseStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#assignmentOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentOperator(@NotNull Java8CommentSupportedParser.AssignmentOperatorContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#labeledStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabeledStatementNoShortIf(@NotNull Java8CommentSupportedParser.LabeledStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#catches}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatches(@NotNull Java8CommentSupportedParser.CatchesContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodReference_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodReference_lfno_primary(@NotNull Java8CommentSupportedParser.MethodReference_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#variableDeclaratorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaratorList(@NotNull Java8CommentSupportedParser.VariableDeclaratorListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#variableModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableModifier(@NotNull Java8CommentSupportedParser.VariableModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#castExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastExpression(@NotNull Java8CommentSupportedParser.CastExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#conditionalOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalOrExpression(@NotNull Java8CommentSupportedParser.ConditionalOrExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeParameterModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameterModifier(@NotNull Java8CommentSupportedParser.TypeParameterModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#elementValuePair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValuePair(@NotNull Java8CommentSupportedParser.ElementValuePairContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#floatingPointType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatingPointType(@NotNull Java8CommentSupportedParser.FloatingPointTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#dimExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimExpr(@NotNull Java8CommentSupportedParser.DimExprContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannInterfaceType_lf_unannClassOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannInterfaceType_lf_unannClassOrInterfaceType(@NotNull Java8CommentSupportedParser.UnannInterfaceType_lf_unannClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#resource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource(@NotNull Java8CommentSupportedParser.ResourceContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclusiveOrExpression(@NotNull Java8CommentSupportedParser.InclusiveOrExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMethodModifier(@NotNull Java8CommentSupportedParser.InterfaceMethodModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#resourceSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceSpecification(@NotNull Java8CommentSupportedParser.ResourceSpecificationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#interfaceTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceTypeList(@NotNull Java8CommentSupportedParser.InterfaceTypeListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#ifThenElseStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfThenElseStatementNoShortIf(@NotNull Java8CommentSupportedParser.IfThenElseStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannInterfaceType(@NotNull Java8CommentSupportedParser.UnannInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#interfaceModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceModifier(@NotNull Java8CommentSupportedParser.InterfaceModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusiveOrExpression(@NotNull Java8CommentSupportedParser.ExclusiveOrExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassType(@NotNull Java8CommentSupportedParser.ClassTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#postIncrementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostIncrementExpression(@NotNull Java8CommentSupportedParser.PostIncrementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#tryStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTryStatement(@NotNull Java8CommentSupportedParser.TryStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#elementValueList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValueList(@NotNull Java8CommentSupportedParser.ElementValueListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#basicForStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasicForStatementNoShortIf(@NotNull Java8CommentSupportedParser.BasicForStatementNoShortIfContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#typeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDeclaration(@NotNull Java8CommentSupportedParser.TypeDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#switchStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchStatement(@NotNull Java8CommentSupportedParser.SwitchStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#wildcard}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWildcard(@NotNull Java8CommentSupportedParser.WildcardContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDeclaration(@NotNull Java8CommentSupportedParser.ClassDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unaryExpressionNotPlusMinus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpressionNotPlusMinus(@NotNull Java8CommentSupportedParser.UnaryExpressionNotPlusMinusContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#unannReferenceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnannReferenceType(@NotNull Java8CommentSupportedParser.UnannReferenceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodHeader}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodHeader(@NotNull Java8CommentSupportedParser.MethodHeaderContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#catchFormalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchFormalParameter(@NotNull Java8CommentSupportedParser.CatchFormalParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#enumConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstant(@NotNull Java8CommentSupportedParser.EnumConstantContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodInvocation_lfno_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodInvocation_lfno_primary(@NotNull Java8CommentSupportedParser.MethodInvocation_lfno_primaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#packageName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageName(@NotNull Java8CommentSupportedParser.PackageNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#importDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportDeclaration(@NotNull Java8CommentSupportedParser.ImportDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#primitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveType(@NotNull Java8CommentSupportedParser.PrimitiveTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceDeclaration(@NotNull Java8CommentSupportedParser.InterfaceDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#localVariableDeclarationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocalVariableDeclarationStatement(@NotNull Java8CommentSupportedParser.LocalVariableDeclarationStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#blockStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStatement(@NotNull Java8CommentSupportedParser.BlockStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#classType_lf_classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassType_lf_classOrInterfaceType(@NotNull Java8CommentSupportedParser.ClassType_lf_classOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#packageOrTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageOrTypeName(@NotNull Java8CommentSupportedParser.PackageOrTypeNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(@NotNull Java8CommentSupportedParser.AssignmentContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodName(@NotNull Java8CommentSupportedParser.MethodNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#statementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementExpression(@NotNull Java8CommentSupportedParser.StatementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#breakStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakStatement(@NotNull Java8CommentSupportedParser.BreakStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#ambiguousName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAmbiguousName(@NotNull Java8CommentSupportedParser.AmbiguousNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#statementExpressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementExpressionList(@NotNull Java8CommentSupportedParser.StatementExpressionListContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMethodDeclaration(@NotNull Java8CommentSupportedParser.InterfaceMethodDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#throws_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThrows_(@NotNull Java8CommentSupportedParser.Throws_Context ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#switchLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchLabel(@NotNull Java8CommentSupportedParser.SwitchLabelContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#methodReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodReference(@NotNull Java8CommentSupportedParser.MethodReferenceContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#primaryNoNewArray}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryNoNewArray(@NotNull Java8CommentSupportedParser.PrimaryNoNewArrayContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#numericType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericType(@NotNull Java8CommentSupportedParser.NumericTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link Java8CommentSupportedParser#continueStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinueStatement(@NotNull Java8CommentSupportedParser.ContinueStatementContext ctx);
}