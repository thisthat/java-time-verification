package IntermediateModelHelper.indexing;

import IntermediateModelHelper.envirorment.Env;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTRE;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModel.visitors.interfaces.ParseIM;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 * This class created the {@link MTMetric} for a given {@link ASTClass}.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexingFile extends ParseIM {

	String lastMethodName = "";
	List<String> signatureLastMethodName = new ArrayList<>();
	MTMetric data;
	String anonymousClass = "";
	Stack<String> stackAnonymousClasses = new Stack<>();
	ASTClass _c = null;

	public static MTMetric getMetrics(){
		return new MTMetric();
	}


	/**
	 * Start the indexing of a {@link ASTClass}.
	 * It goes through the methods of it and then through their statements.
	 * @param c	Class to analyze
	 * @return	The index data structure of the class.
	 */
	public MTMetric index(ASTClass c) {
		this._c = c;
		data = new MTMetric();
		data.setMethods(c.getName(), c.getMethods().size());
		super.start(c);
		return data;
	}

	@Override
	protected void analyzeASTRE(ASTRE r, Env env) {

	}
}