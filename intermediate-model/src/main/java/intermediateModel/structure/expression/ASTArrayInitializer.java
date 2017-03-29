package intermediateModel.structure.expression;

import intermediateModel.interfaces.ASTREVisitor;
import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTArrayInitializer extends IASTStm implements IASTRE {

	List<IASTRE> exprs = new ArrayList<>();

	public ASTArrayInitializer(int start, int end, List<IASTRE> exprs) {
		super(start, end);
		this.exprs = exprs;
	}

	public List<IASTRE> getExprs() {
		return exprs;
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTArrayInitializer(this);
		for(IASTRE e : exprs){
			e.visit(visitor);
		}
		visitor.exitASTArrayInitializer(this);
		visitor.exitAll(this);
	}

	@Override
	public String print() {
		StringBuffer bf = new StringBuffer();
		bf.append("{");
		for(IASTRE e : exprs){
			bf.append(e.print());
			bf.append(",");
		}
		String out = bf.toString();
		return out.substring(0, out.length()-1) + " }";
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTArrayInitializer(this);
		for(IASTRE e : exprs){
			e.visit(visitor);
		}
		visitor.exitASTArrayInitializer(this);
		visitor.exitAll(this);
	}
}
