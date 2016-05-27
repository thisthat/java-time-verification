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

		@Override
		public List<IASTStm> getStms() {
			return stms;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ASTTryBranch)) return false;

			ASTTryBranch tryBranch = (ASTTryBranch) o;

			if (getStms() != null ? !getStms().equals(tryBranch.getStms()) : tryBranch.getStms() != null) return false;

			return true;
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

		@Override
		public List<IASTStm> getStms() {
			return stms;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ASTCatchBranch)) return false;

			ASTCatchBranch that = (ASTCatchBranch) o;

			if (getStms() != null ? !getStms().equals(that.getStms()) : that.getStms() != null) return false;
			if (expr != null ? !expr.equals(that.expr) : that.expr != null) return false;

			return true;
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

		@Override
		public List<IASTStm> getStms() {
			return stms;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ASTFinallyBranch)) return false;

			ASTFinallyBranch that = (ASTFinallyBranch) o;

			if (getStms() != null ? !getStms().equals(that.getStms()) : that.getStms() != null) return false;

			return true;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTTry)) return false;

		ASTTry astTry = (ASTTry) o;

		if (getTryBranch() != null ? !getTryBranch().equals(astTry.getTryBranch()) : astTry.getTryBranch() != null)
			return false;
		if (getCatchBranch() != null ? !getCatchBranch().equals(astTry.getCatchBranch()) : astTry.getCatchBranch() != null)
			return false;
		if (getFinallyBranch() != null ? !getFinallyBranch().equals(astTry.getFinallyBranch()) : astTry.getFinallyBranch() != null)
			return false;

		return true;
	}

}
