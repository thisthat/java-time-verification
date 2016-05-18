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
public class ASTTry extends IASTStm {

	public class ASTTryBranch extends IASTStm implements IASTHasStms {
		List<IASTStm> stms = new ArrayList<>();
		public ASTTryBranch(Token start, Token end) {
			super(start, end);
		}

		@Override
		public void addStms(IASTStm stm) {
			this.stms.add(stm);
		}

		@Override
		public String toString() {
			String out = "tryBranch \n";
			for(IASTStm e : stms){
				out += e.toString() + "\n";
			}
			out += "enbTryBranch \n";
			return out;
		}
	}

	public class ASTCatchBranch extends IASTStm implements IASTHasStms {
		List<IASTStm> stms = new ArrayList<>();
		ASTVariable expr = null;
		public ASTCatchBranch(Token start, Token end, ASTVariable expr) {
			super(start, end);
			this.expr = expr;
		}

		@Override
		public void addStms(IASTStm stm) {
			this.stms.add(stm);
		}

		@Override
		public String toString() {
			String out = "catchBranch (";
			if(expr != null)
				out += expr.toString();
			out += ")\n";
			for(IASTStm e : stms){
				out += e.toString() + "\n";
			}
			out += "enbCatchBranch \n";
			return out;
		}
	}

	public class ASTFinallyBranch extends IASTStm implements IASTHasStms {
		List<IASTStm> stms = new ArrayList<>();
		public ASTFinallyBranch(Token start, Token end) {
			super(start, end);
		}

		@Override
		public void addStms(IASTStm stm) {
			this.stms.add(stm);
		}
		@Override
		public String toString() {
			String out = "finallyBranch \n";
			for(IASTStm e : stms){
				out += e.toString() + "\n";
			}
			out += "enbFinallyBranch \n";
			return out;
		}
	}

	ASTTryBranch tryBranch = null;
	List<ASTCatchBranch> catchBranch = new ArrayList<>();
	ASTFinallyBranch finallyBranch = null;
	public ASTTry(Token start, Token end) {
		super(start, end);
	}

	@Override
	public String toString() {
		String out = "try \n";
		if(tryBranch != null)
			out += tryBranch.toString();
		if(catchBranch.size() > 0)
			for(ASTCatchBranch c : catchBranch)
				out += c.toString();
		if(finallyBranch != null)
			out += finallyBranch.toString();
		out += "endTry \n";
		return out;
	}

	public ASTTryBranch getTryBranch() {
		return tryBranch;
	}

	public void setTryBranch(ASTTryBranch tryBranch) {
		this.tryBranch = tryBranch;
	}

	public List<ASTCatchBranch> getCatchBranch() {
		return catchBranch;
	}

	public void setCatchBranch(List<ASTCatchBranch> catchBranch) {
		this.catchBranch = catchBranch;
	}
	public void addCatchBranch(ASTCatchBranch catchBranch) {
		this.catchBranch.add(catchBranch);
	}

	public ASTFinallyBranch getFinallyBranch() {
		return finallyBranch;
	}

	public void setFinallyBranch(ASTFinallyBranch finallyBranch) {
		this.finallyBranch = finallyBranch;
	}
}
