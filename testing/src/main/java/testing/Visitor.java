package testing;

import org.eclipse.jdt.core.dom.*;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Visitor extends ASTVisitor {

	@Override
	public boolean visit(ClassInstanceCreation node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(CompilationUnit node) {
		//return super.visit(node);
		List imports = node.imports();
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		SimpleName nn = node.getName();
		String n = nn.toString();
		//System.out.println(n);
		return true;
	}
	@Override
	public boolean visit(MethodDeclaration node) {
		System.out.println("Method " + node.extraDimensions() + " is visited");
		return true;
	}
}
