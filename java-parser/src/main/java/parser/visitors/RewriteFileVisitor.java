package parser.visitors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import parser.grammar.Java8CommentSupportedBaseListener;
import parser.grammar.Java8CommentSupportedParser;

/**
 * Dummy example to check all the parser.grammar elements
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */

public class RewriteFileVisitor extends Java8CommentSupportedBaseListener {

    String output = "";
    public String getOutput(){
        return output;
    }

    @Override
    public void enterClassMemberDeclaration(@NotNull Java8CommentSupportedParser.ClassMemberDeclarationContext ctx) {
        super.enterClassMemberDeclaration(ctx);
    }

    @Override
    public void enterStatementNoShortIf(@NotNull Java8CommentSupportedParser.StatementNoShortIfContext ctx) {
        super.enterStatementNoShortIf(ctx);
    }

    @Override
    public void enterAnnotationTypeElementDeclaration(@NotNull Java8CommentSupportedParser.AnnotationTypeElementDeclarationContext ctx) {
        super.enterAnnotationTypeElementDeclaration(ctx);
    }

    @Override
    public void enterMethodInvocation_lf_primary(@NotNull Java8CommentSupportedParser.MethodInvocation_lf_primaryContext ctx) {
        super.enterMethodInvocation_lf_primary(ctx);
    }

    @Override
    public void enterPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryContext ctx) {
        super.enterPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary(ctx);
    }

    @Override
    public void enterAnnotationTypeBody(@NotNull Java8CommentSupportedParser.AnnotationTypeBodyContext ctx) {
        super.enterAnnotationTypeBody(ctx);
    }

    @Override
    public void enterConstantModifier(@NotNull Java8CommentSupportedParser.ConstantModifierContext ctx) {
        super.enterConstantModifier(ctx);
    }

    @Override
    public void enterLambdaBody(@NotNull Java8CommentSupportedParser.LambdaBodyContext ctx) {
        super.enterLambdaBody(ctx);
    }

    @Override
    public void enterArgumentList(@NotNull Java8CommentSupportedParser.ArgumentListContext ctx) {
        super.enterArgumentList(ctx);
    }

    @Override
    public void enterClassBodyDeclaration(@NotNull Java8CommentSupportedParser.ClassBodyDeclarationContext ctx) {
        super.enterClassBodyDeclaration(ctx);
    }

    @Override
    public void enterClassInstanceCreationExpression_lfno_primary(@NotNull Java8CommentSupportedParser.ClassInstanceCreationExpression_lfno_primaryContext ctx) {
        super.enterClassInstanceCreationExpression_lfno_primary(ctx);
    }

    @Override
    public void enterPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryContext ctx) {
        super.enterPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary(ctx);
    }

    @Override
    public void enterUnaryExpression(@NotNull Java8CommentSupportedParser.UnaryExpressionContext ctx) {
        super.enterUnaryExpression(ctx);
    }

    @Override
    public void enterClassType_lfno_classOrInterfaceType(@NotNull Java8CommentSupportedParser.ClassType_lfno_classOrInterfaceTypeContext ctx) {
        super.enterClassType_lfno_classOrInterfaceType(ctx);
    }

    @Override
    public void enterArrayType(@NotNull Java8CommentSupportedParser.ArrayTypeContext ctx) {
        super.enterArrayType(ctx);
    }

    @Override
    public void enterSimpleTypeName(@NotNull Java8CommentSupportedParser.SimpleTypeNameContext ctx) {
        super.enterSimpleTypeName(ctx);
    }

    @Override
    public void enterExpressionName(@NotNull Java8CommentSupportedParser.ExpressionNameContext ctx) {
        super.enterExpressionName(ctx);
    }

    @Override
    public void enterStatementWithoutTrailingSubstatement(@NotNull Java8CommentSupportedParser.StatementWithoutTrailingSubstatementContext ctx) {
        super.enterStatementWithoutTrailingSubstatement(ctx);
    }

    @Override
    public void enterConstructorDeclarator(@NotNull Java8CommentSupportedParser.ConstructorDeclaratorContext ctx) {
        super.enterConstructorDeclarator(ctx);
    }

    @Override
    public void enterAssertStatement(@NotNull Java8CommentSupportedParser.AssertStatementContext ctx) {
        super.enterAssertStatement(ctx);
    }

    @Override
    public void enterArrayCreationExpression(@NotNull Java8CommentSupportedParser.ArrayCreationExpressionContext ctx) {
        super.enterArrayCreationExpression(ctx);
    }

    @Override
    public void enterUnannArrayType(@NotNull Java8CommentSupportedParser.UnannArrayTypeContext ctx) {
        super.enterUnannArrayType(ctx);
    }

    @Override
    public void enterVariableDeclaratorId(@NotNull Java8CommentSupportedParser.VariableDeclaratorIdContext ctx) {
        super.enterVariableDeclaratorId(ctx);
    }

    @Override
    public void enterTryWithResourcesStatement(@NotNull Java8CommentSupportedParser.TryWithResourcesStatementContext ctx) {
        super.enterTryWithResourcesStatement(ctx);
    }

    @Override
    public void enterTypeArguments(@NotNull Java8CommentSupportedParser.TypeArgumentsContext ctx) {
        super.enterTypeArguments(ctx);
    }

    @Override
    public void enterExceptionType(@NotNull Java8CommentSupportedParser.ExceptionTypeContext ctx) {
        super.enterExceptionType(ctx);
    }

    @Override
    public void enterExceptionTypeList(@NotNull Java8CommentSupportedParser.ExceptionTypeListContext ctx) {
        super.enterExceptionTypeList(ctx);
    }

    @Override
    public void enterAdditiveExpression(@NotNull Java8CommentSupportedParser.AdditiveExpressionContext ctx) {
        super.enterAdditiveExpression(ctx);
    }

    @Override
    public void enterRelationalExpression(@NotNull Java8CommentSupportedParser.RelationalExpressionContext ctx) {
        super.enterRelationalExpression(ctx);
    }

    @Override
    public void enterReferenceType(@NotNull Java8CommentSupportedParser.ReferenceTypeContext ctx) {
        super.enterReferenceType(ctx);
    }

    @Override
    public void enterArrayAccess_lf_primary(@NotNull Java8CommentSupportedParser.ArrayAccess_lf_primaryContext ctx) {
        super.enterArrayAccess_lf_primary(ctx);
    }

    @Override
    public void enterInferredFormalParameterList(@NotNull Java8CommentSupportedParser.InferredFormalParameterListContext ctx) {
        super.enterInferredFormalParameterList(ctx);
    }

    @Override
    public void enterReturnStatement(@NotNull Java8CommentSupportedParser.ReturnStatementContext ctx) {
        super.enterReturnStatement(ctx);
    }

    @Override
    public void enterTypeImportOnDemandDeclaration(@NotNull Java8CommentSupportedParser.TypeImportOnDemandDeclarationContext ctx) {
        super.enterTypeImportOnDemandDeclaration(ctx);
    }

    @Override
    public void enterTypeParameters(@NotNull Java8CommentSupportedParser.TypeParametersContext ctx) {
        super.enterTypeParameters(ctx);
    }

    @Override
    public void enterLastFormalParameter(@NotNull Java8CommentSupportedParser.LastFormalParameterContext ctx) {
        super.enterLastFormalParameter(ctx);
    }

    @Override
    public void enterLiteral(@NotNull Java8CommentSupportedParser.LiteralContext ctx) {
        super.enterLiteral(ctx);
    }

    @Override
    public void enterResult(@NotNull Java8CommentSupportedParser.ResultContext ctx) {
        super.enterResult(ctx);
    }

    @Override
    public void enterFieldAccess_lfno_primary(@NotNull Java8CommentSupportedParser.FieldAccess_lfno_primaryContext ctx) {
        super.enterFieldAccess_lfno_primary(ctx);
    }

    @Override
    public void enterVariableDeclarator(@NotNull Java8CommentSupportedParser.VariableDeclaratorContext ctx) {
        super.enterVariableDeclarator(ctx);
    }

    @Override
    public void enterFinally_(@NotNull Java8CommentSupportedParser.Finally_Context ctx) {
        super.enterFinally_(ctx);
    }

    @Override
    public void enterClassBody(@NotNull Java8CommentSupportedParser.ClassBodyContext ctx) {
        super.enterClassBody(ctx);
    }

    @Override
    public void enterTypeArgument(@NotNull Java8CommentSupportedParser.TypeArgumentContext ctx) {
        super.enterTypeArgument(ctx);
    }

    @Override
    public void enterUnannPrimitiveType(@NotNull Java8CommentSupportedParser.UnannPrimitiveTypeContext ctx) {
        super.enterUnannPrimitiveType(ctx);
    }

    @Override
    public void enterExplicitConstructorInvocation(@NotNull Java8CommentSupportedParser.ExplicitConstructorInvocationContext ctx) {
        super.enterExplicitConstructorInvocation(ctx);
    }

    @Override
    public void enterEnumBody(@NotNull Java8CommentSupportedParser.EnumBodyContext ctx) {
        super.enterEnumBody(ctx);
    }

    @Override
    public void enterAdditionalBound(@NotNull Java8CommentSupportedParser.AdditionalBoundContext ctx) {
        super.enterAdditionalBound(ctx);
    }

    @Override
    public void enterPrimaryNoNewArray_lfno_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lfno_primaryContext ctx) {
        super.enterPrimaryNoNewArray_lfno_primary(ctx);
    }

    @Override
    public void enterUnannClassOrInterfaceType(@NotNull Java8CommentSupportedParser.UnannClassOrInterfaceTypeContext ctx) {
        super.enterUnannClassOrInterfaceType(ctx);
    }

    @Override
    public void enterIfThenStatement(@NotNull Java8CommentSupportedParser.IfThenStatementContext ctx) {
        super.enterIfThenStatement(ctx);
    }

    @Override
    public void enterPostDecrementExpression_lf_postfixExpression(@NotNull Java8CommentSupportedParser.PostDecrementExpression_lf_postfixExpressionContext ctx) {
        super.enterPostDecrementExpression_lf_postfixExpression(ctx);
    }

    @Override
    public void enterInterfaceType(@NotNull Java8CommentSupportedParser.InterfaceTypeContext ctx) {
        super.enterInterfaceType(ctx);
    }

    @Override
    public void enterMethodReference_lf_primary(@NotNull Java8CommentSupportedParser.MethodReference_lf_primaryContext ctx) {
        super.enterMethodReference_lf_primary(ctx);
    }

    @Override
    public void enterAndExpression(@NotNull Java8CommentSupportedParser.AndExpressionContext ctx) {
        super.enterAndExpression(ctx);
    }

    @Override
    public void enterEnumConstantModifier(@NotNull Java8CommentSupportedParser.EnumConstantModifierContext ctx) {
        super.enterEnumConstantModifier(ctx);
    }

    @Override
    public void enterUnannClassType_lfno_unannClassOrInterfaceType(@NotNull Java8CommentSupportedParser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx) {
        super.enterUnannClassType_lfno_unannClassOrInterfaceType(ctx);
    }

    @Override
    public void enterStatement(@NotNull Java8CommentSupportedParser.StatementContext ctx) {
        super.enterStatement(ctx);
    }

    @Override
    public void enterFieldModifier(@NotNull Java8CommentSupportedParser.FieldModifierContext ctx) {
        super.enterFieldModifier(ctx);
    }

    @Override
    public void enterMarkerAnnotation(@NotNull Java8CommentSupportedParser.MarkerAnnotationContext ctx) {
        super.enterMarkerAnnotation(ctx);
    }

    @Override
    public void enterResourceList(@NotNull Java8CommentSupportedParser.ResourceListContext ctx) {
        super.enterResourceList(ctx);
    }

    @Override
    public void enterArrayAccess_lfno_primary(@NotNull Java8CommentSupportedParser.ArrayAccess_lfno_primaryContext ctx) {
        super.enterArrayAccess_lfno_primary(ctx);
    }

    @Override
    public void enterStaticInitializer(@NotNull Java8CommentSupportedParser.StaticInitializerContext ctx) {
        super.enterStaticInitializer(ctx);
    }

    @Override
    public void enterConditionalExpression(@NotNull Java8CommentSupportedParser.ConditionalExpressionContext ctx) {
        super.enterConditionalExpression(ctx);
    }

    @Override
    public void enterFieldDeclaration(@NotNull Java8CommentSupportedParser.FieldDeclarationContext ctx) {
        super.enterFieldDeclaration(ctx);
    }

    @Override
    public void enterLeftHandSide(@NotNull Java8CommentSupportedParser.LeftHandSideContext ctx) {
        super.enterLeftHandSide(ctx);
    }

    @Override
    public void enterBasicForStatement(@NotNull Java8CommentSupportedParser.BasicForStatementContext ctx) {
        super.enterBasicForStatement(ctx);
    }

    @Override
    public void enterWhileStatement(@NotNull Java8CommentSupportedParser.WhileStatementContext ctx) {
        super.enterWhileStatement(ctx);
    }

    @Override
    public void enterPackageDeclaration(@NotNull Java8CommentSupportedParser.PackageDeclarationContext ctx) {
        super.enterPackageDeclaration(ctx);
    }

    @Override
    public void enterLocalVariableDeclaration(@NotNull Java8CommentSupportedParser.LocalVariableDeclarationContext ctx) {
        super.enterLocalVariableDeclaration(ctx);
    }

    @Override
    public void enterSuperinterfaces(@NotNull Java8CommentSupportedParser.SuperinterfacesContext ctx) {
        super.enterSuperinterfaces(ctx);
    }

    @Override
    public void enterInterfaceMemberDeclaration(@NotNull Java8CommentSupportedParser.InterfaceMemberDeclarationContext ctx) {
        super.enterInterfaceMemberDeclaration(ctx);
    }

    @Override
    public void enterClassInstanceCreationExpression_lf_primary(@NotNull Java8CommentSupportedParser.ClassInstanceCreationExpression_lf_primaryContext ctx) {
        super.enterClassInstanceCreationExpression_lf_primary(ctx);
    }

    @Override
    public void enterSwitchBlock(@NotNull Java8CommentSupportedParser.SwitchBlockContext ctx) {
        super.enterSwitchBlock(ctx);
    }

    @Override
    public void enterForInit(@NotNull Java8CommentSupportedParser.ForInitContext ctx) {
        super.enterForInit(ctx);
    }

    @Override
    public void enterBlockStatements(@NotNull Java8CommentSupportedParser.BlockStatementsContext ctx) {
        super.enterBlockStatements(ctx);
    }

    @Override
    public void enterFormalParameters(@NotNull Java8CommentSupportedParser.FormalParametersContext ctx) {
        super.enterFormalParameters(ctx);
    }

    @Override
    public void enterComment(@NotNull Java8CommentSupportedParser.CommentContext ctx) {
        super.enterComment(ctx);
    }

    @Override
    public void enterTypeArgumentsOrDiamond(@NotNull Java8CommentSupportedParser.TypeArgumentsOrDiamondContext ctx) {
        super.enterTypeArgumentsOrDiamond(ctx);
    }

    @Override
    public void enterPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryContext ctx) {
        super.enterPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary(ctx);
    }

    @Override
    public void enterDefaultValue(@NotNull Java8CommentSupportedParser.DefaultValueContext ctx) {
        super.enterDefaultValue(ctx);
    }

    @Override
    public void enterType(@NotNull Java8CommentSupportedParser.TypeContext ctx) {
        super.enterType(ctx);
    }

    @Override
    public void enterElementValuePairList(@NotNull Java8CommentSupportedParser.ElementValuePairListContext ctx) {
        super.enterElementValuePairList(ctx);
    }

    @Override
    public void enterSynchronizedStatement(@NotNull Java8CommentSupportedParser.SynchronizedStatementContext ctx) {
        super.enterSynchronizedStatement(ctx);
    }

    @Override
    public void enterUnannClassType_lf_unannClassOrInterfaceType(@NotNull Java8CommentSupportedParser.UnannClassType_lf_unannClassOrInterfaceTypeContext ctx) {
        super.enterUnannClassType_lf_unannClassOrInterfaceType(ctx);
    }

    @Override
    public void enterSuperclass(@NotNull Java8CommentSupportedParser.SuperclassContext ctx) {
        super.enterSuperclass(ctx);
    }

    @Override
    public void enterBlock(@NotNull Java8CommentSupportedParser.BlockContext ctx) {
        super.enterBlock(ctx);
    }

    @Override
    public void enterExpressionStatement(@NotNull Java8CommentSupportedParser.ExpressionStatementContext ctx) {
        super.enterExpressionStatement(ctx);
    }

    @Override
    public void enterPreIncrementExpression(@NotNull Java8CommentSupportedParser.PreIncrementExpressionContext ctx) {
        super.enterPreIncrementExpression(ctx);
    }

    @Override
    public void enterEnumBodyDeclarations(@NotNull Java8CommentSupportedParser.EnumBodyDeclarationsContext ctx) {
        super.enterEnumBodyDeclarations(ctx);
    }

    @Override
    public void enterDimExprs(@NotNull Java8CommentSupportedParser.DimExprsContext ctx) {
        super.enterDimExprs(ctx);
    }

    @Override
    public void enterForUpdate(@NotNull Java8CommentSupportedParser.ForUpdateContext ctx) {
        super.enterForUpdate(ctx);
    }

    @Override
    public void enterEmptyStatement(@NotNull Java8CommentSupportedParser.EmptyStatementContext ctx) {
        super.enterEmptyStatement(ctx);
    }

    @Override
    public void enterPrimaryNoNewArray_lf_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lf_primaryContext ctx) {
        super.enterPrimaryNoNewArray_lf_primary(ctx);
    }

    @Override
    public void enterAnnotationTypeElementModifier(@NotNull Java8CommentSupportedParser.AnnotationTypeElementModifierContext ctx) {
        super.enterAnnotationTypeElementModifier(ctx);
    }

    @Override
    public void enterShiftExpression(@NotNull Java8CommentSupportedParser.ShiftExpressionContext ctx) {
        super.enterShiftExpression(ctx);
    }

    @Override
    public void enterFieldAccess_lf_primary(@NotNull Java8CommentSupportedParser.FieldAccess_lf_primaryContext ctx) {
        super.enterFieldAccess_lf_primary(ctx);
    }

    @Override
    public void enterInstanceInitializer(@NotNull Java8CommentSupportedParser.InstanceInitializerContext ctx) {
        super.enterInstanceInitializer(ctx);
    }

    @Override
    public void enterUnannType(@NotNull Java8CommentSupportedParser.UnannTypeContext ctx) {
        super.enterUnannType(ctx);
    }

    @Override
    public void enterIntegralType(@NotNull Java8CommentSupportedParser.IntegralTypeContext ctx) {
        super.enterIntegralType(ctx);
    }

    @Override
    public void enterPostIncrementExpression_lf_postfixExpression(@NotNull Java8CommentSupportedParser.PostIncrementExpression_lf_postfixExpressionContext ctx) {
        super.enterPostIncrementExpression_lf_postfixExpression(ctx);
    }

    @Override
    public void enterClassOrInterfaceType(@NotNull Java8CommentSupportedParser.ClassOrInterfaceTypeContext ctx) {
        super.enterClassOrInterfaceType(ctx);
    }

    @Override
    public void enterEqualityExpression(@NotNull Java8CommentSupportedParser.EqualityExpressionContext ctx) {
        super.enterEqualityExpression(ctx);
    }

    @Override
    public void enterNormalAnnotation(@NotNull Java8CommentSupportedParser.NormalAnnotationContext ctx) {
        super.enterNormalAnnotation(ctx);
    }

    @Override
    public void enterTypeBound(@NotNull Java8CommentSupportedParser.TypeBoundContext ctx) {
        super.enterTypeBound(ctx);
    }

    @Override
    public void enterPrimary(@NotNull Java8CommentSupportedParser.PrimaryContext ctx) {
        super.enterPrimary(ctx);
    }

    @Override
    public void enterClassModifier(@NotNull Java8CommentSupportedParser.ClassModifierContext ctx) {
        super.enterClassModifier(ctx);
    }

    @Override
    public void enterFieldAccess(@NotNull Java8CommentSupportedParser.FieldAccessContext ctx) {
        super.enterFieldAccess(ctx);
    }

    @Override
    public void enterTypeName(@NotNull Java8CommentSupportedParser.TypeNameContext ctx) {
        super.enterTypeName(ctx);
    }

    @Override
    public void enterConstructorBody(@NotNull Java8CommentSupportedParser.ConstructorBodyContext ctx) {
        super.enterConstructorBody(ctx);
    }

    @Override
    public void enterCatchClause(@NotNull Java8CommentSupportedParser.CatchClauseContext ctx) {
        super.enterCatchClause(ctx);
    }

    @Override
    public void enterPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryContext ctx) {
        super.enterPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary(ctx);
    }

    @Override
    public void enterLabeledStatement(@NotNull Java8CommentSupportedParser.LabeledStatementContext ctx) {
        super.enterLabeledStatement(ctx);
    }

    @Override
    public void enterSwitchLabels(@NotNull Java8CommentSupportedParser.SwitchLabelsContext ctx) {
        super.enterSwitchLabels(ctx);
    }

    @Override
    public void enterFormalParameter(@NotNull Java8CommentSupportedParser.FormalParameterContext ctx) {
        super.enterFormalParameter(ctx);
    }

    @Override
    public void enterInterfaceType_lf_classOrInterfaceType(@NotNull Java8CommentSupportedParser.InterfaceType_lf_classOrInterfaceTypeContext ctx) {
        super.enterInterfaceType_lf_classOrInterfaceType(ctx);
    }

    @Override
    public void enterSingleTypeImportDeclaration(@NotNull Java8CommentSupportedParser.SingleTypeImportDeclarationContext ctx) {
        super.enterSingleTypeImportDeclaration(ctx);
    }

    @Override
    public void enterSingleElementAnnotation(@NotNull Java8CommentSupportedParser.SingleElementAnnotationContext ctx) {
        super.enterSingleElementAnnotation(ctx);
    }

    @Override
    public void enterElementValueArrayInitializer(@NotNull Java8CommentSupportedParser.ElementValueArrayInitializerContext ctx) {
        super.enterElementValueArrayInitializer(ctx);
    }

    @Override
    public void enterConstantExpression(@NotNull Java8CommentSupportedParser.ConstantExpressionContext ctx) {
        super.enterConstantExpression(ctx);
    }

    @Override
    public void enterForStatement(@NotNull Java8CommentSupportedParser.ForStatementContext ctx) {
        super.enterForStatement(ctx);
    }

    @Override
    public void enterTypeArgumentList(@NotNull Java8CommentSupportedParser.TypeArgumentListContext ctx) {
        super.enterTypeArgumentList(ctx);
    }

    @Override
    public void enterPostDecrementExpression(@NotNull Java8CommentSupportedParser.PostDecrementExpressionContext ctx) {
        super.enterPostDecrementExpression(ctx);
    }

    @Override
    public void enterReceiverParameter(@NotNull Java8CommentSupportedParser.ReceiverParameterContext ctx) {
        super.enterReceiverParameter(ctx);
    }

    @Override
    public void enterWhileStatementNoShortIf(@NotNull Java8CommentSupportedParser.WhileStatementNoShortIfContext ctx) {
        super.enterWhileStatementNoShortIf(ctx);
    }

    @Override
    public void enterEnumConstantList(@NotNull Java8CommentSupportedParser.EnumConstantListContext ctx) {
        super.enterEnumConstantList(ctx);
    }

    @Override
    public void enterDoStatement(@NotNull Java8CommentSupportedParser.DoStatementContext ctx) {
        super.enterDoStatement(ctx);
    }

    @Override
    public void enterCatchType(@NotNull Java8CommentSupportedParser.CatchTypeContext ctx) {
        super.enterCatchType(ctx);
    }

    @Override
    public void enterForStatementNoShortIf(@NotNull Java8CommentSupportedParser.ForStatementNoShortIfContext ctx) {
        super.enterForStatementNoShortIf(ctx);
    }

    @Override
    public void enterIfThenElseStatement(@NotNull Java8CommentSupportedParser.IfThenElseStatementContext ctx) {
        super.enterIfThenElseStatement(ctx);
    }

    @Override
    public void enterAssignmentOperator(@NotNull Java8CommentSupportedParser.AssignmentOperatorContext ctx) {
        super.enterAssignmentOperator(ctx);
    }

    @Override
    public void enterLabeledStatementNoShortIf(@NotNull Java8CommentSupportedParser.LabeledStatementNoShortIfContext ctx) {
        super.enterLabeledStatementNoShortIf(ctx);
    }

    @Override
    public void enterCatches(@NotNull Java8CommentSupportedParser.CatchesContext ctx) {
        super.enterCatches(ctx);
    }

    @Override
    public void enterMethodReference_lfno_primary(@NotNull Java8CommentSupportedParser.MethodReference_lfno_primaryContext ctx) {
        super.enterMethodReference_lfno_primary(ctx);
    }

    @Override
    public void enterVariableDeclaratorList(@NotNull Java8CommentSupportedParser.VariableDeclaratorListContext ctx) {
        super.enterVariableDeclaratorList(ctx);
    }

    @Override
    public void enterVariableModifier(@NotNull Java8CommentSupportedParser.VariableModifierContext ctx) {
        super.enterVariableModifier(ctx);
    }

    @Override
    public void enterCastExpression(@NotNull Java8CommentSupportedParser.CastExpressionContext ctx) {
        super.enterCastExpression(ctx);
    }

    @Override
    public void enterConditionalOrExpression(@NotNull Java8CommentSupportedParser.ConditionalOrExpressionContext ctx) {
        super.enterConditionalOrExpression(ctx);
    }

    @Override
    public void enterTypeParameterModifier(@NotNull Java8CommentSupportedParser.TypeParameterModifierContext ctx) {
        super.enterTypeParameterModifier(ctx);
    }

    @Override
    public void enterElementValuePair(@NotNull Java8CommentSupportedParser.ElementValuePairContext ctx) {
        super.enterElementValuePair(ctx);
    }

    @Override
    public void enterFloatingPointType(@NotNull Java8CommentSupportedParser.FloatingPointTypeContext ctx) {
        super.enterFloatingPointType(ctx);
    }

    @Override
    public void enterDimExpr(@NotNull Java8CommentSupportedParser.DimExprContext ctx) {
        super.enterDimExpr(ctx);
    }

    @Override
    public void enterUnannInterfaceType_lf_unannClassOrInterfaceType(@NotNull Java8CommentSupportedParser.UnannInterfaceType_lf_unannClassOrInterfaceTypeContext ctx) {
        super.enterUnannInterfaceType_lf_unannClassOrInterfaceType(ctx);
    }

    @Override
    public void enterResource(@NotNull Java8CommentSupportedParser.ResourceContext ctx) {
        super.enterResource(ctx);
    }

    @Override
    public void enterInclusiveOrExpression(@NotNull Java8CommentSupportedParser.InclusiveOrExpressionContext ctx) {
        super.enterInclusiveOrExpression(ctx);
    }

    @Override
    public void enterInterfaceMethodModifier(@NotNull Java8CommentSupportedParser.InterfaceMethodModifierContext ctx) {
        super.enterInterfaceMethodModifier(ctx);
    }

    @Override
    public void enterResourceSpecification(@NotNull Java8CommentSupportedParser.ResourceSpecificationContext ctx) {
        super.enterResourceSpecification(ctx);
    }

    @Override
    public void enterInterfaceTypeList(@NotNull Java8CommentSupportedParser.InterfaceTypeListContext ctx) {
        super.enterInterfaceTypeList(ctx);
    }

    @Override
    public void enterIfThenElseStatementNoShortIf(@NotNull Java8CommentSupportedParser.IfThenElseStatementNoShortIfContext ctx) {
        super.enterIfThenElseStatementNoShortIf(ctx);
    }

    @Override
    public void enterUnannInterfaceType(@NotNull Java8CommentSupportedParser.UnannInterfaceTypeContext ctx) {
        super.enterUnannInterfaceType(ctx);
    }

    @Override
    public void enterInterfaceModifier(@NotNull Java8CommentSupportedParser.InterfaceModifierContext ctx) {
        super.enterInterfaceModifier(ctx);
    }

    @Override
    public void enterExclusiveOrExpression(@NotNull Java8CommentSupportedParser.ExclusiveOrExpressionContext ctx) {
        super.enterExclusiveOrExpression(ctx);
    }

    @Override
    public void enterClassType(@NotNull Java8CommentSupportedParser.ClassTypeContext ctx) {
        super.enterClassType(ctx);
    }

    @Override
    public void enterPostIncrementExpression(@NotNull Java8CommentSupportedParser.PostIncrementExpressionContext ctx) {
        super.enterPostIncrementExpression(ctx);
    }

    @Override
    public void enterTryStatement(@NotNull Java8CommentSupportedParser.TryStatementContext ctx) {
        super.enterTryStatement(ctx);
    }

    @Override
    public void enterElementValueList(@NotNull Java8CommentSupportedParser.ElementValueListContext ctx) {
        super.enterElementValueList(ctx);
    }

    @Override
    public void enterBasicForStatementNoShortIf(@NotNull Java8CommentSupportedParser.BasicForStatementNoShortIfContext ctx) {
        super.enterBasicForStatementNoShortIf(ctx);
    }

    @Override
    public void enterTypeDeclaration(@NotNull Java8CommentSupportedParser.TypeDeclarationContext ctx) {
        super.enterTypeDeclaration(ctx);
    }

    @Override
    public void enterSwitchStatement(@NotNull Java8CommentSupportedParser.SwitchStatementContext ctx) {
        super.enterSwitchStatement(ctx);
    }

    @Override
    public void enterWildcard(@NotNull Java8CommentSupportedParser.WildcardContext ctx) {
        super.enterWildcard(ctx);
    }

    @Override
    public void enterClassDeclaration(@NotNull Java8CommentSupportedParser.ClassDeclarationContext ctx) {
        super.enterClassDeclaration(ctx);
    }

    @Override
    public void enterUnaryExpressionNotPlusMinus(@NotNull Java8CommentSupportedParser.UnaryExpressionNotPlusMinusContext ctx) {
        super.enterUnaryExpressionNotPlusMinus(ctx);
    }

    @Override
    public void enterUnannReferenceType(@NotNull Java8CommentSupportedParser.UnannReferenceTypeContext ctx) {
        super.enterUnannReferenceType(ctx);
    }

    @Override
    public void enterMethodHeader(@NotNull Java8CommentSupportedParser.MethodHeaderContext ctx) {
        super.enterMethodHeader(ctx);
    }

    @Override
    public void enterCatchFormalParameter(@NotNull Java8CommentSupportedParser.CatchFormalParameterContext ctx) {
        super.enterCatchFormalParameter(ctx);
    }

    @Override
    public void enterEnumConstant(@NotNull Java8CommentSupportedParser.EnumConstantContext ctx) {
        super.enterEnumConstant(ctx);
    }

    @Override
    public void enterMethodInvocation_lfno_primary(@NotNull Java8CommentSupportedParser.MethodInvocation_lfno_primaryContext ctx) {
        super.enterMethodInvocation_lfno_primary(ctx);
    }

    @Override
    public void enterPackageName(@NotNull Java8CommentSupportedParser.PackageNameContext ctx) {
        super.enterPackageName(ctx);
    }

    @Override
    public void enterImportDeclaration(@NotNull Java8CommentSupportedParser.ImportDeclarationContext ctx) {
        super.enterImportDeclaration(ctx);
    }

    @Override
    public void enterPrimitiveType(@NotNull Java8CommentSupportedParser.PrimitiveTypeContext ctx) {
        super.enterPrimitiveType(ctx);
    }

    @Override
    public void enterInterfaceDeclaration(@NotNull Java8CommentSupportedParser.InterfaceDeclarationContext ctx) {
        super.enterInterfaceDeclaration(ctx);
    }

    @Override
    public void enterLocalVariableDeclarationStatement(@NotNull Java8CommentSupportedParser.LocalVariableDeclarationStatementContext ctx) {
        super.enterLocalVariableDeclarationStatement(ctx);
    }

    @Override
    public void enterBlockStatement(@NotNull Java8CommentSupportedParser.BlockStatementContext ctx) {
        super.enterBlockStatement(ctx);
    }

    @Override
    public void enterClassType_lf_classOrInterfaceType(@NotNull Java8CommentSupportedParser.ClassType_lf_classOrInterfaceTypeContext ctx) {
        super.enterClassType_lf_classOrInterfaceType(ctx);
    }

    @Override
    public void enterPackageOrTypeName(@NotNull Java8CommentSupportedParser.PackageOrTypeNameContext ctx) {
        super.enterPackageOrTypeName(ctx);
    }

    @Override
    public void enterAssignment(@NotNull Java8CommentSupportedParser.AssignmentContext ctx) {
        super.enterAssignment(ctx);
    }

    @Override
    public void enterMethodName(@NotNull Java8CommentSupportedParser.MethodNameContext ctx) {
        super.enterMethodName(ctx);
    }

    @Override
    public void enterStatementExpression(@NotNull Java8CommentSupportedParser.StatementExpressionContext ctx) {
        super.enterStatementExpression(ctx);
    }

    @Override
    public void enterBreakStatement(@NotNull Java8CommentSupportedParser.BreakStatementContext ctx) {
        super.enterBreakStatement(ctx);
    }

    @Override
    public void enterAmbiguousName(@NotNull Java8CommentSupportedParser.AmbiguousNameContext ctx) {
        super.enterAmbiguousName(ctx);
    }

    @Override
    public void enterUnannInterfaceType_lfno_unannClassOrInterfaceType(@NotNull Java8CommentSupportedParser.UnannInterfaceType_lfno_unannClassOrInterfaceTypeContext ctx) {
        super.enterUnannInterfaceType_lfno_unannClassOrInterfaceType(ctx);
    }

    @Override
    public void enterEnumDeclaration(@NotNull Java8CommentSupportedParser.EnumDeclarationContext ctx) {
        super.enterEnumDeclaration(ctx);
    }

    @Override
    public void enterConstantDeclaration(@NotNull Java8CommentSupportedParser.ConstantDeclarationContext ctx) {
        super.enterConstantDeclaration(ctx);
    }

    @Override
    public void enterPostfixExpression(@NotNull Java8CommentSupportedParser.PostfixExpressionContext ctx) {
        super.enterPostfixExpression(ctx);
    }

    @Override
    public void enterAnnotation(@NotNull Java8CommentSupportedParser.AnnotationContext ctx) {
        super.enterAnnotation(ctx);
    }

    @Override
    public void enterVariableInitializer(@NotNull Java8CommentSupportedParser.VariableInitializerContext ctx) {
        super.enterVariableInitializer(ctx);
    }

    @Override
    public void enterStaticImportOnDemandDeclaration(@NotNull Java8CommentSupportedParser.StaticImportOnDemandDeclarationContext ctx) {
        super.enterStaticImportOnDemandDeclaration(ctx);
    }

    @Override
    public void enterExpression(@NotNull Java8CommentSupportedParser.ExpressionContext ctx) {
        super.enterExpression(ctx);
    }

    @Override
    public void enterThrowStatement(@NotNull Java8CommentSupportedParser.ThrowStatementContext ctx) {
        super.enterThrowStatement(ctx);
    }

    @Override
    public void enterMethodInvocation(@NotNull Java8CommentSupportedParser.MethodInvocationContext ctx) {
        super.enterMethodInvocation(ctx);
    }

    @Override
    public void enterSingleStaticImportDeclaration(@NotNull Java8CommentSupportedParser.SingleStaticImportDeclarationContext ctx) {
        super.enterSingleStaticImportDeclaration(ctx);
    }

    @Override
    public void enterLambdaParameters(@NotNull Java8CommentSupportedParser.LambdaParametersContext ctx) {
        super.enterLambdaParameters(ctx);
    }

    @Override
    public void enterConditionalAndExpression(@NotNull Java8CommentSupportedParser.ConditionalAndExpressionContext ctx) {
        super.enterConditionalAndExpression(ctx);
    }

    @Override
    public void enterMultiplicativeExpression(@NotNull Java8CommentSupportedParser.MultiplicativeExpressionContext ctx) {
        super.enterMultiplicativeExpression(ctx);
    }

    @Override
    public void enterPackageModifier(@NotNull Java8CommentSupportedParser.PackageModifierContext ctx) {
        super.enterPackageModifier(ctx);
    }

    @Override
    public void enterConstructorDeclaration(@NotNull Java8CommentSupportedParser.ConstructorDeclarationContext ctx) {
        super.enterConstructorDeclaration(ctx);
    }

    @Override
    public void enterPrimaryNoNewArray_lfno_arrayAccess(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lfno_arrayAccessContext ctx) {
        super.enterPrimaryNoNewArray_lfno_arrayAccess(ctx);
    }

    @Override
    public void enterUnannTypeVariable(@NotNull Java8CommentSupportedParser.UnannTypeVariableContext ctx) {
        super.enterUnannTypeVariable(ctx);
    }

    @Override
    public void enterNormalInterfaceDeclaration(@NotNull Java8CommentSupportedParser.NormalInterfaceDeclarationContext ctx) {
        super.enterNormalInterfaceDeclaration(ctx);
    }

    @Override
    public void enterInterfaceType_lfno_classOrInterfaceType(@NotNull Java8CommentSupportedParser.InterfaceType_lfno_classOrInterfaceTypeContext ctx) {
        super.enterInterfaceType_lfno_classOrInterfaceType(ctx);
    }

    @Override
    public void enterConstructorModifier(@NotNull Java8CommentSupportedParser.ConstructorModifierContext ctx) {
        super.enterConstructorModifier(ctx);
    }

    @Override
    public void enterEnumConstantName(@NotNull Java8CommentSupportedParser.EnumConstantNameContext ctx) {
        super.enterEnumConstantName(ctx);
    }

    @Override
    public void enterClassInstanceCreationExpression(@NotNull Java8CommentSupportedParser.ClassInstanceCreationExpressionContext ctx) {
        super.enterClassInstanceCreationExpression(ctx);
    }

    @Override
    public void enterMethodDeclarator(@NotNull Java8CommentSupportedParser.MethodDeclaratorContext ctx) {
        super.enterMethodDeclarator(ctx);
    }

    @Override
    public void enterAnnotationTypeMemberDeclaration(@NotNull Java8CommentSupportedParser.AnnotationTypeMemberDeclarationContext ctx) {
        super.enterAnnotationTypeMemberDeclaration(ctx);
    }

    @Override
    public void enterPreDecrementExpression(@NotNull Java8CommentSupportedParser.PreDecrementExpressionContext ctx) {
        super.enterPreDecrementExpression(ctx);
    }

    @Override
    public void enterVariableInitializerList(@NotNull Java8CommentSupportedParser.VariableInitializerListContext ctx) {
        super.enterVariableInitializerList(ctx);
    }

    @Override
    public void enterExtendsInterfaces(@NotNull Java8CommentSupportedParser.ExtendsInterfacesContext ctx) {
        super.enterExtendsInterfaces(ctx);
    }

    @Override
    public void enterElementValue(@NotNull Java8CommentSupportedParser.ElementValueContext ctx) {
        super.enterElementValue(ctx);
    }

    @Override
    public void enterArrayInitializer(@NotNull Java8CommentSupportedParser.ArrayInitializerContext ctx) {
        super.enterArrayInitializer(ctx);
    }

    @Override
    public void enterArrayAccess(@NotNull Java8CommentSupportedParser.ArrayAccessContext ctx) {
        super.enterArrayAccess(ctx);
    }

    @Override
    public void enterMethodModifier(@NotNull Java8CommentSupportedParser.MethodModifierContext ctx) {
        super.enterMethodModifier(ctx);
    }

    @Override
    public void enterUnannClassType(@NotNull Java8CommentSupportedParser.UnannClassTypeContext ctx) {
        super.enterUnannClassType(ctx);
    }

    @Override
    public void enterLambdaExpression(@NotNull Java8CommentSupportedParser.LambdaExpressionContext ctx) {
        super.enterLambdaExpression(ctx);
    }

    @Override
    public void enterAssignmentExpression(@NotNull Java8CommentSupportedParser.AssignmentExpressionContext ctx) {
        super.enterAssignmentExpression(ctx);
    }

    @Override
    public void enterTypeParameterList(@NotNull Java8CommentSupportedParser.TypeParameterListContext ctx) {
        super.enterTypeParameterList(ctx);
    }

    @Override
    public void enterNormalClassDeclaration(@NotNull Java8CommentSupportedParser.NormalClassDeclarationContext ctx) {
        super.enterNormalClassDeclaration(ctx);
    }

    @Override
    public void enterFormalParameterList(@NotNull Java8CommentSupportedParser.FormalParameterListContext ctx) {
        super.enterFormalParameterList(ctx);
    }

    @Override
    public void enterEnhancedForStatementNoShortIf(@NotNull Java8CommentSupportedParser.EnhancedForStatementNoShortIfContext ctx) {
        super.enterEnhancedForStatementNoShortIf(ctx);
    }

    @Override
    public void enterAnnotationTypeDeclaration(@NotNull Java8CommentSupportedParser.AnnotationTypeDeclarationContext ctx) {
        super.enterAnnotationTypeDeclaration(ctx);
    }

    @Override
    public void enterCompilationUnit(@NotNull Java8CommentSupportedParser.CompilationUnitContext ctx) {
        super.enterCompilationUnit(ctx);
    }

    @Override
    public void enterWildcardBounds(@NotNull Java8CommentSupportedParser.WildcardBoundsContext ctx) {
        super.enterWildcardBounds(ctx);
    }

    @Override
    public void enterPrimaryNoNewArray_lf_arrayAccess(@NotNull Java8CommentSupportedParser.PrimaryNoNewArray_lf_arrayAccessContext ctx) {
        super.enterPrimaryNoNewArray_lf_arrayAccess(ctx);
    }

    @Override
    public void enterEnhancedForStatement(@NotNull Java8CommentSupportedParser.EnhancedForStatementContext ctx) {
        super.enterEnhancedForStatement(ctx);
    }

    @Override
    public void enterSwitchBlockStatementGroup(@NotNull Java8CommentSupportedParser.SwitchBlockStatementGroupContext ctx) {
        super.enterSwitchBlockStatementGroup(ctx);
    }

    @Override
    public void enterTypeVariable(@NotNull Java8CommentSupportedParser.TypeVariableContext ctx) {
        super.enterTypeVariable(ctx);
    }

    @Override
    public void enterTypeParameter(@NotNull Java8CommentSupportedParser.TypeParameterContext ctx) {
        super.enterTypeParameter(ctx);
    }

    @Override
    public void enterMethodDeclaration(@NotNull Java8CommentSupportedParser.MethodDeclarationContext ctx) {
        super.enterMethodDeclaration(ctx);
    }

    @Override
    public void enterInterfaceBody(@NotNull Java8CommentSupportedParser.InterfaceBodyContext ctx) {
        super.enterInterfaceBody(ctx);
    }

    @Override
    public void enterMethodBody(@NotNull Java8CommentSupportedParser.MethodBodyContext ctx) {
        super.enterMethodBody(ctx);
    }

    @Override
    public void enterDims(@NotNull Java8CommentSupportedParser.DimsContext ctx) {
        super.enterDims(ctx);
    }

    @Override
    public void enterStatementExpressionList(@NotNull Java8CommentSupportedParser.StatementExpressionListContext ctx) {
        super.enterStatementExpressionList(ctx);
    }

    @Override
    public void enterInterfaceMethodDeclaration(@NotNull Java8CommentSupportedParser.InterfaceMethodDeclarationContext ctx) {
        super.enterInterfaceMethodDeclaration(ctx);
    }

    @Override
    public void enterThrows_(@NotNull Java8CommentSupportedParser.Throws_Context ctx) {
        super.enterThrows_(ctx);
    }

    @Override
    public void enterSwitchLabel(@NotNull Java8CommentSupportedParser.SwitchLabelContext ctx) {
        super.enterSwitchLabel(ctx);
    }

    @Override
    public void enterMethodReference(@NotNull Java8CommentSupportedParser.MethodReferenceContext ctx) {
        super.enterMethodReference(ctx);
    }

    @Override
    public void enterPrimaryNoNewArray(@NotNull Java8CommentSupportedParser.PrimaryNoNewArrayContext ctx) {
        super.enterPrimaryNoNewArray(ctx);
    }

    @Override
    public void enterNumericType(@NotNull Java8CommentSupportedParser.NumericTypeContext ctx) {
        super.enterNumericType(ctx);
    }

    @Override
    public void enterContinueStatement(@NotNull Java8CommentSupportedParser.ContinueStatementContext ctx) {
        super.enterContinueStatement(ctx);
    }

    @Override
    public void enterEveryRule(@NotNull ParserRuleContext ctx) {
        String tmp = "RULE " + ctx.getClass() + " -- " + ctx.getText();
        System.out.println(tmp);
        output += tmp + "\n";
        super.enterEveryRule(ctx);
    }
}
