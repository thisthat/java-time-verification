package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.ASTVisitor;
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
	boolean isArray = false;

	public ASTNewObject(Token start, Token end, String typeName, boolean isArray) {
		super(start, end);
		this.typeName = typeName;
		this.isArray = isArray;
	}

	public ASTNewObject(int start, int end, String typeName, boolean isArray) {
		super(start, end);
		this.typeName = typeName;
		this.isArray = isArray;
	}

	public ASTNewObject(Token start, Token end, String typeName, boolean isArray, List<IASTRE> parameters) {
		super(start, end);
		this.typeName = typeName;
		this.isArray = isArray;
		this.parameters = parameters;

	}

	public ASTNewObject(int start, int end, String typeName, boolean isArray, List<IASTRE> parameters) {
		super(start, end);
		this.typeName = typeName;
		this.isArray = isArray;
		this.parameters = parameters;
	}

	public boolean isArray() {
		return isArray;
	}

	public void setArray(boolean array) {
		isArray = array;
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
		visitor.enterAll(this);
		visitor.enterASTNewObject(this);
		for(IASTRE p : parameters){
			p.visit(visitor);
		}
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTNewObject(this);
		for(IASTRE p : parameters){
			p.visit(visitor);
		}
	}
}

