package intermediateModel.interfaces;


import intermediateModel.structure.ASTVariable;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IASTMethod extends IASTHasStms, IASTVisitor {
	List<ASTVariable> getParameters();
	String getName();
}
