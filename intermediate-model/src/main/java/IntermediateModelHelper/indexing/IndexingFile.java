package IntermediateModelHelper.indexing;

import IntermediateModelHelper.envirorment.Env;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.visitors.creation.JDTVisitor;
import IntermediateModel.visitors.interfaces.ParseIM;

import java.io.File;
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

	public static MTMetric getMetrics(File _old, File _new, List<SourceCodeChange> changes){
		List<ASTClass> _oldClasses = JDTVisitor.parse(_old);
		List<ASTClass> _newClasses = JDTVisitor.parse(_new);
		SourceCodeChange c = changes.get(0);

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