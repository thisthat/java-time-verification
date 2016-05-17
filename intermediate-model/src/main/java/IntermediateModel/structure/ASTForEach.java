package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.expression.ASTVar;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTForEach  extends IASTStm implements IASTHasStms {
	ASTVariable var;
	ASTRE expr;
	List<IASTStm> stms = new ArrayList<IASTStm>();
	public ASTForEach(Token start, Token end) {
		super(start, end);
	}

	public ASTForEach(Token start, Token end, ASTVariable var, ASTRE expr) {
		super(start, end);
		this.var = var;
		this.expr = expr;
	}

	public ASTVariable getVar() {
		return var;
	}

	public void setVar(ASTVariable var) {
		this.var = var;
	}

	public ASTRE getExpr() {
		return expr;
	}

	public void setExpr(ASTRE expr) {
		this.expr = expr;
	}

	@Override
	public void addStms(IASTStm stm) {
		this.stms.add(stm);
	}

	@Override
	public String toString() {
		String out = "foreach(";
		out += var.toString() + " : ";
		out += expr.toString() + " )\n";
		for(IASTStm e : stms){
			out += e.toString() + "\n";
		}
		out += "\nendForEach";
		return out;
	}
}
