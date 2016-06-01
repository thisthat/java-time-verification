package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTStm;
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

	public ASTForEach(int start, int end) {
		super(start, end);
	}

	public ASTForEach(int start, int end, ASTVariable var, ASTRE expr) {
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
	public List<IASTStm> getStms() {
		return stms;
	}

	@Override
	public String toString() {
		String out = "";
		//out += var.toString() + " : ";
		//out += expr.toString() + " )\n";
		for(IASTStm e : stms){
			out += e.toString() + "\n";
		}
		//out += "\nendForEach";
		return out;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTForEach)) return false;

		ASTForEach that = (ASTForEach) o;

		if (getVar() != null ? !getVar().equals(that.getVar()) : that.getVar() != null) return false;
		if (getExpr() != null ? !getExpr().equals(that.getExpr()) : that.getExpr() != null) return false;
		if (getStms() != null ? !getStms().equals(that.getStms()) : that.getStms() != null) return false;

		return true;
	}

}
