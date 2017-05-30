package intermediateModel.interfaces;


import intermediateModel.structure.ASTVariable;
import org.javatuples.Pair;

import javax.lang.model.type.DeclaredType;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IASTMethod extends IASTHasStms, IASTVisitor {
	List<ASTVariable> getParameters();
	String getName();
	List<String> getExceptionsThrowed();
	String getReturnType();
	List<String> getSignature();
	int getLine();
	int getLineEnd();
	int getStart();
	int getEnd();
	boolean equalsBySignature(IASTMethod c);
	boolean equalsBySignature(String pkg, String name, List<Pair<String,String>> signature);
	boolean isStatic();
	List<DeclaredVar> getDeclaredVar();
	void setDeclaredVars();
}
