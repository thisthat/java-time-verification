package IntermediateModel.interfaces;


import IntermediateModel.structure.ASTVariable;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IASTMethod extends IASTHasStms{
	List<ASTVariable> getParameters();
	String getName();
}
