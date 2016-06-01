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
public class ASTSynchronized extends IASTStm implements IASTHasStms {
	List<IASTStm> stms = new ArrayList<IASTStm>();;
	ASTRE expr;

	public ASTSynchronized(Token start, Token end) {
		super(start, end);
	}

	public ASTSynchronized(Token start, Token end, ASTRE expr) {
		super(start, end);
		this.expr = expr;
	}

		public ASTSynchronized(int start, int end) {
		super(start, end);
	}

	public ASTSynchronized(int start, int end, ASTRE expr) {
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
		String out = "";
		for(IASTStm e : stms){
			out += e.toString() + "\n";
		}
		return out;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTSynchronized)) return false;

		ASTSynchronized that = (ASTSynchronized) o;

		if (getStms() != null ? !getStms().equals(that.getStms()) : that.getStms() != null) return false;
		if (getExpr() != null ? !getExpr().equals(that.getExpr()) : that.getExpr() != null) return false;

		return true;
	}

}
