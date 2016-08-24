package IntermediateModelHelper.envirorment;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.structure.expression.*;
import intermediateModel.visitors.ParseRE;

import java.util.Objects;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
class ResolveExpressionType extends ParseRE {

	final String _boolean = "boolean";
	final String _int = "int";
	final String _string = "String";

	protected ResolveExpressionType(Env e) {
		super(e);
	}

	public String getType(IASTRE r){
		return (String) analyze(r);
	}

	@Override
	protected Object analyze(ASTArrayInitializer r) {
		if(r.getExprs().size() > 0){
			return analyze(r.getExprs().get(0));
		}
		return null;
	}

	@Override
	protected Object analyze(ASTAssignment r) {
		return analyze(r.getLeft());
	}

	@Override
	protected Object analyze(ASTCast r) {
		return r.getType();
	}

	@Override
	protected Object analyze(ASTConditional r) {
		return analyze(r.getThenExpr());
	}

	@Override
	protected Object analyze(ASTNewObject r) {
		return r.getTypeName();
	}

	@Override
	protected Object analyze(ASTPostOp r) {
		return _int;
	}

	@Override
	protected Object analyze(ASTPreOp r) {
		return _int;
	}


	@Override
	protected Object analyze(ASTVariableDeclaration r) {
		return r.getType();
	}

	@Override
	protected Object analyze(ASTVariableMultipleDeclaration r) {
		return r.getType();
	}


	@Override
	protected Object analyze(ASTAttributeAccess r) {
		return analyze(r.getVariableName());
	}

	@Override
	protected Object analyze(ASTBinary r) {
		String type = null;
		switch (r.getOp()){
			case and:
			case or:
			case greater:
			case greaterEqual:
			case less:
			case lessEqual:
			case instanceOf:
			case equality:
			case not:
				type = _boolean; break;
			case plus:
				Object left = analyze(r.getLeft());
				Object right = analyze(r.getRight());
				if(left == null || right == null){
					return null;
				}
				if(left.equals(_string) || right.equals(_string)){
					type = _string;
				} else {
					type = _int;
				}
				break;
			default: type = _int; break;
		}
		return type;
	}

	@Override
	protected Object analyze(ASTUnary r) {
		if(r.getOp() == IASTRE.OPERATOR.not){
			return _boolean;
		} else {
			return _int;
		}
	}

	@Override
	protected Object analyze(ASTMethodCall r) {
		String method = r.getMethodName();
		if(e.existMethod(method)){
			return e.getMethod(method).getRetType();
		}
		return null;
	}

	@Override
	protected Object analyze(ASTLiteral r) {
		String var_name = r.getValue();
		return e.getVar(var_name) == null ? null : e.getVar(var_name).getType();
	}


}