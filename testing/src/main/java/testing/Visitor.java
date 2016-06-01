package testing;

import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTImport;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Visitor extends ASTVisitor {

	public List<ASTClass> listOfClasses = new ArrayList<ASTClass>();
	private ASTClass lastClass;
	private IASTHasStms lastMethod;
	private String packageName = "";
	private List<ASTImport> listOfImports = new ArrayList<>();



	@Override
	public boolean visit(CompilationUnit node) {
		//return super.visit(node);
		List imports = node.imports();
		for(Object i: imports){
			if(i instanceof ImportDeclaration){
				ImportDeclaration ii = (ImportDeclaration) i;
				ASTImport imp = new ASTImport(ii.getStartPosition(), ii.getStartPosition() + ii.getLength(), ii.isStatic(), ii.getName().getFullyQualifiedName());
				listOfImports.add(imp);
			}
		}
		System.err.println(Arrays.toString(listOfImports.toArray()));
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		SimpleName nn = node.getName();
		String n = nn.toString();
		System.out.println("CLASS:" + n);
		return true;
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		SimpleName nn = node.getName();
		String n = nn.toString();
		System.out.println("END CLASS:" + n);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		System.out.println("Method " + node.getName() + " is visited");
		return true;
	}
}
