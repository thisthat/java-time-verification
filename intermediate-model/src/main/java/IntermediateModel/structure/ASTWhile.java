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
public class ASTWhile extends IASTStm implements IASTHasStms {
	List<IASTStm> stms = new ArrayList<IASTStm>();;
	ASTRE expr;

	public ASTWhile(Token start, Token end) {
		super(start.getStartIndex(), end.getStopIndex());
	}

	public ASTWhile(Token start, Token end, ASTRE expr) {
		super(start.getStartIndex(), end.getStopIndex());
		this.expr = expr;
	}

	public ASTWhile(int start, int end) {
		super(start, end);
	}

	public ASTWhile(int start, int end, ASTRE expr) {
		super(start, end);
		this.expr = expr;
	}


	public List<IASTStm> getStms() {
		return stms;
	}

	public void setStms(List<IASTStm> stms) {
		this.stms = stms;
	}

	public ASTRE getExpr() {
		return expr;
	}

	public void setExpr(ASTRE expr) {
		this.expr = expr;
	}

	@Override
	public void addStms(IASTStm stm) {
		stms.add(stm);
	}

	@Override
	public String toString() {
		String out = "while(";
		out += expr.toString();
		out += ")\n";
		for(IASTStm e : stms){
			out += e.toString() + "\n";
		}
		out += "\nendWhile";
		return out;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTWhile)) return false;

		ASTWhile astWhile = (ASTWhile) o;

		if (getStms() != null ? !getStms().equals(astWhile.getStms()) : astWhile.getStms() != null) return false;
		if (getExpr() != null ? !getExpr().equals(astWhile.getExpr()) : astWhile.getExpr() != null) return false;

		return true;
	}
}
