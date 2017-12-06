package smt;

import intermediateModel.structure.expression.*;
import slicing.model.Assignment;
import slicing.model.Method;
import slicing.model.visitor.DefaultReducedVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 10/07/2017.
 */
public class VariableExtractor extends DefaultReducedVisitor {

    List<String> vars = new ArrayList<>();
    public static List<String> extract(Method m){
        VariableExtractor extractor = new VariableExtractor();
        m.visit(extractor);
        return extractor.getVars();
    }

    private VariableExtractor() {
    }

    public List<String> getVars() {
        return vars;
    }

    private void add(String name){
        if(vars.contains(name)) return;
        if(name.matches("[-+]?\\d*\\.?\\d+")) return;
        if(name.startsWith("\"")) return;
        vars.add(name);
    }

    @Override
    public void enterAssignmet(Assignment elm) {
        add(elm.getLeft());
    }

    @Override
    public void enterASTArrayInitializer(ASTArrayInitializer elm) {
        add(elm.getIdentifier());
    }

    @Override
    public void enterASTAssignment(ASTAssignment elm) {
        if(elm.getLeft() instanceof ASTLiteral)
            add(((ASTLiteral) elm.getLeft()).getValue());
    }

    @Override
    public void enterASTAttributeAccess(ASTAttributeAccess elm) {
        add(elm.getAttributeName());
    }

    @Override
    public void enterASTbinary(ASTBinary elm) {
        if(elm.getLeft() instanceof ASTLiteral)
            add(((ASTLiteral) elm.getLeft()).getValue());
        if(elm.getRight() instanceof ASTLiteral)
            add(((ASTLiteral) elm.getRight()).getValue());
    }

    @Override
    public void enterASTLiteral(ASTLiteral elm) {
        //super.enterASTLiteral(elm);
    }

    @Override
    public void enterASTMethodCall(ASTMethodCall elm) {
        if(elm.getExprCallee() instanceof  ASTLiteral){
            add(((ASTLiteral) elm.getExprCallee()).getValue());
        }
    }

    @Override
    public void enterASTPostOp(ASTPostOp elm) {
        if(elm.getVar() instanceof ASTLiteral) {
            add(((ASTLiteral) elm.getVar()).getValue());
        }
    }

    @Override
    public void enterASTPreOp(ASTPreOp elm) {
        if(elm.getVar() instanceof ASTLiteral) {
            add(((ASTLiteral) elm.getVar()).getValue());
        }
    }

    @Override
    public void enterASTUnary(ASTUnary elm) {
        if(elm.getExpr() instanceof ASTLiteral) {
            add(((ASTLiteral) elm.getExpr()).getValue());
        }
    }

    @Override
    public void enterASTVariableDeclaration(ASTVariableDeclaration elm) {
        add(elm.getNameString());
    }
}
