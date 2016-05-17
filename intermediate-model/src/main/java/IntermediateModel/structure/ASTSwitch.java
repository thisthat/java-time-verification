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
public class ASTSwitch extends IASTStm {

	public class ASTCase extends IASTStm implements IASTHasStms{
		List<String> labels = new ArrayList<>();
		List<IASTStm> stms = new ArrayList<>();

		public ASTCase(Token start, Token end, String label) {
			super(start, end);
			this.labels.add(label);
		}

		public ASTCase(Token start, Token end, List<String> labels) {
			super(start, end);
			this.labels = labels;
		}

		@Override
		public void addStms(IASTStm stm) {
			this.stms.add(stm);
		}

		public void addStms(String label) {
			this.labels.add(label);
		}

		@Override
		public String toString() {
			String out = "";
			for(String label : labels)
				out += "case " + label + ": \n";
			for(IASTStm e : stms){
				out += e.toString() + "\n";
			}
			out += "\nendCase\n";
			return out;
		}

	}


	ASTRE expr;
	List<ASTCase> cases = new ArrayList<>();
	public ASTSwitch(Token start, Token end, ASTRE expr) {
		super(start, end);
		this.expr = expr;
	}

	public void addCase(ASTCase stm){
		this.cases.add(stm);
	}

	public ASTRE getExpr() {
		return expr;
	}

	public void setExpr(ASTRE expr) {
		this.expr = expr;
	}

	@Override
	public String toString() {
		String out = "switch(";
		out += expr.toString();
		out += ")\n";
		for(ASTCase e : cases){
			out += e.toString() + "\n";
		}
		out += "\nendSwitch\n";
		return out;
	}
}
