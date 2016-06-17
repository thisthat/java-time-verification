package IntermediateModel.structure;

import IntermediateModel.interfaces.ASTVisitor;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.IASTVisitor;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTImport extends IASTStm implements IASTVisitor {
	private boolean isStatic = false;
	String packagename = "";

	public ASTImport(Token start, Token end, boolean isStatic, String packagename) {
		super(start,end);
		this.isStatic = isStatic;
		this.packagename = packagename;
	}
	public ASTImport(int start, int end, boolean isStatic, String packagename) {
		super(start,end);
		this.isStatic = isStatic;
		this.packagename = packagename;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean aStatic) {
		isStatic = aStatic;
	}

	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

	@Override
	public String toString() {
		return "import " + (isStatic ? "static " : "") + packagename + ";\n";
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTImport(this);
	}
}
