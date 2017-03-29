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
public class ASTVariableMultipleDeclaration extends IASTStm implements IASTRE {

	String type;
	List<IASTRE> vars = new ArrayList<>();

	public ASTVariableMultipleDeclaration(int start, int end, String type, List<IASTRE> vars) {
		super(start, end);
		this.type = type;
		this.vars = vars;
	}

	public void addVar(ASTVariableDeclaration v){
		vars.add(v);
	}

	public String getType() {
		return type;
	}

	public List<IASTRE> getVars() {
		return vars;
	}

	public void setVars(List<IASTRE> vars) {
		this.vars = vars;
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		//Default RE
		visitor.enterAll(this);
		visitor.enterASTVariableMultipleDeclaration(this);
		for(IASTRE v : vars){
			v.visit(visitor);
		}
		visitor.exitASTVariableMultipleDeclaration(this);
		visitor.exitAll(this);
	}

	@Override
	public String print() {
		StringBuffer bf = new StringBuffer();
		bf.append(bf + " ");
		for(IASTRE v : vars){
			bf.append(v.print());
			bf.append(",");
		}
		bf.subSequence(0, bf.length()-1);
		return bf.toString();
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visit((ASTREVisitor) visitor);
	}

}
