package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.ASTREVisitor;
import org.antlr.v4.runtime.Token;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTNewObject extends IASTStm implements IASTRE {

	List<IASTRE> parameters;
	String typeName;

	public ASTNewObject(Token start, Token end, String typeName) {
		super(start, end);
		this.typeName = typeName;
	}

	public ASTNewObject(int start, int end, String typeName) {
		super(start, end);
		this.typeName = typeName;
	}

	public ASTNewObject(Token start, Token end, String typeName, List<IASTRE> parameters) {
		super(start, end);
		this.typeName = typeName;
		this.parameters = parameters;
	}

	public ASTNewObject(int start, int end, String typeName, List<IASTRE> parameters) {
		super(start, end);
		this.typeName = typeName;
		this.parameters = parameters;
	}

	public List<IASTRE> getParameters() {
		return parameters;
	}

	public void setParameters(List<IASTRE> parameters) {
		this.parameters = parameters;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public String toString() {
		return "ASTNewObject{" +
				"parameters=" + parameters +
				", typeName='" + typeName + '\'' +
				'}';
	}


	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterASTNewObject(this);
		for(IASTRE p : parameters){
			p.visit(visitor);
		}
	}
}

