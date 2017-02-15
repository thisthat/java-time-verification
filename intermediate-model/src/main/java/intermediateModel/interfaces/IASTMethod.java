package IntermediateModel.interfaces;


import IntermediateModel.structure.ASTVariable;
import org.javatuples.Pair;

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
	int getStart();
	int getEnd();
	boolean equalsBySignature(IASTMethod c);
	boolean equalsBySignature(String pkg, String name, List<Pair<String,String>> signature);
	boolean isStatic();
}
