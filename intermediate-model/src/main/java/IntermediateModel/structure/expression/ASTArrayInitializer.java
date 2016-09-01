package intermediateModel.structure.expression;

import intermediateModel.interfaces.ASTREVisitor;
import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTArrayInitializer extends IASTStm implements IASTRE {

	List<IASTRE> exprs = new ArrayList<>();

	public ASTArrayInitializer(Token start, Token end, List<IASTRE> exprs) {
		super(start, end);
		this.exprs = exprs;
	}

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
