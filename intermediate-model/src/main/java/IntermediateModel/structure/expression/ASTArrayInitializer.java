package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.ASTREVisitor;
import IntermediateModel.interfaces.ASTVisitor;
import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
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

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterASTArrayInitializer(this);
		for(IASTRE e : exprs){
			e.visit(visitor);
		}
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTArrayInitializer(this);
		for(IASTRE e : exprs){
			e.visit(visitor);
		}
	}
}
