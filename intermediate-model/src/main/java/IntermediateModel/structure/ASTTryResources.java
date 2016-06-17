package IntermediateModel.structure;

import IntermediateModel.interfaces.ASTVisitor;
import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.IASTVisitor;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTTryResources extends ASTTry implements IASTVisitor {

	List<ASTRE> resources;
	public ASTTryResources(Token start, Token end, List<ASTRE> resources) {
		super(start, end);
		this.resources = resources;
	}
	public ASTTryResources(int start, int end, List<ASTRE> resources) {
		super(start, end);
		this.resources = resources;
	}

	public List<ASTRE> getResources() {
		return resources;
	}

	@Override
	public String toString() {
		String out = "";
		for(ASTRE r : resources)
			out += r.toString();
		out += "\n";
		if(tryBranch != null)
			out += tryBranch.toString();
		if(catchBranch.size() > 0)
			for(ASTCatchBranch c : catchBranch)
				out += c.toString();
		if(finallyBranch != null)
			out += finallyBranch.toString();
		out += "";
		return out;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTTryResources)) return false;
		if (!super.equals(o)) return false;

		ASTTryResources that = (ASTTryResources) o;

		if (resources != null ? !resources.equals(that.resources) : that.resources != null) return false;

		return true;
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTTryResources(this);
		for(ASTRE r : resources){
			r.visit(visitor);
		}
		super.visit(visitor);
	}
}
