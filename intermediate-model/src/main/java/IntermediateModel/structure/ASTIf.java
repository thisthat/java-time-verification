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
public class ASTIf extends IASTStm {

	public class ASTIfStms extends IASTStm implements IASTHasStms {
		List<IASTStm> stms = new ArrayList<>();
		public ASTIfStms(Token start, Token end) {
			super(start, end);
		}

		@Override
		public void addStms(IASTStm stm) {
			this.stms.add(stm);
		}

		@Override
		public String toString() {
			String out = "ifBranch \n";
			for(IASTStm e : stms){
				out += e.toString() + "\n";
			}
			out += "enbIfBranch \n";
			return out;
		}
	}

	public class ASTElseStms extends IASTStm implements IASTHasStms {
		List<IASTStm> stms = new ArrayList<>();
		public ASTElseStms(Token start, Token end) {
			super(start, end);
		}

		@Override
		public void addStms(IASTStm stm) {
			this.stms.add(stm);
		}

		@Override
		public String toString() {
			String out = "elseBranch \n";
			for(IASTStm e : stms){
				out += e.toString() + "\n";
			}
			out += "enbElseBranch \n";
			return out;
		}
	}

	protected ASTIfStms ifBranch = null;
	protected ASTElseStms elseBranch = null;
	protected ASTRE guard = null;

	public ASTIf(Token start, Token end, ASTRE guard) {
		super(start, end);
		this.guard = guard;
	}

	@Override
	public String toString() {
		String out = "if( ";
		out += guard.toString() + " )\n";
		if(ifBranch != null)
			out += ifBranch.toString();
		if(elseBranch != null)
			out += elseBranch.toString();
		out += "endIf \n";
		return out;
	}

	public ASTIfStms getIfBranch() {
		return ifBranch;
	}

	public ASTElseStms getElseBranch() {
		return elseBranch;
	}

	public void setIfBranch(ASTIfStms ifBranch) {
		this.ifBranch = ifBranch;
	}

	public void setElseBranch(ASTElseStms elseBranch) {
		this.elseBranch = elseBranch;
	}
}
