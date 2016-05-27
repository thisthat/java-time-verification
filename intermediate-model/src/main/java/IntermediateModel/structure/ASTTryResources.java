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
public class ASTTryResources extends ASTTry {

	List<ASTRE> resources;
	public ASTTryResources(Token start, Token end, List<ASTRE> resources) {
		super(start, end);
		this.resources = resources;
	}

	@Override
	public String toString() {
		String out = "try(";
		for(ASTRE r : resources)
			out += r.toString();
		out += ")\n";
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTTryResources)) return false;
		if (!super.equals(o)) return false;

		ASTTryResources that = (ASTTryResources) o;

		if (resources != null ? !resources.equals(that.resources) : that.resources != null) return false;

		return true;
	}

}