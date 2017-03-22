package PCFG.creation;

import PCFG.creation.helper.CalculateSyncBlock;
import PCFG.creation.helper.CalculateSyncCall;
import PCFG.creation.helper.CreatePCFG;
import PCFG.structure.PCFG;
import PCFG.structure.node.Node;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTAttribute;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModelHelper.indexing.IndexingFile;
import intermediateModelHelper.indexing.IndexingSyncBlock;
import intermediateModelHelper.indexing.structure.IndexSyncBlock;
import org.javatuples.KeyValue;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * The following class convert the IM of a set of methods in a CFG first.
 * Then, it create cross links between the CFGs of the methods where there is:
 * <ul>
 *     <li>a synchronization on the same variables</li>
 *     <li>a method call on the same synchronized methods</li>
 * </ul>
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IM2CFG {

	CreatePCFG pcfgBuilder = new CreatePCFG();

	public IM2CFG(IASTMethod m, ASTClass c) {
		pcfgBuilder.addMethod(m, c);
	}

	public IM2CFG() {
	}

	public int getConstraintsSize() {
		return pcfgBuilder.getConstraints().size();
	}

	/**
	 * Insert a class in the list of classes to process for creating the PCFG.
	 * Moreover, the method will index the class and extract the time constraint in it.
	 * @param c				Class to analyze
	 * @param method		Method of the class to analyze
	 */
	public void addClass(ASTClass c, IASTMethod method){

		List<Triplet<String,IASTStm,Class>> currentClassConstraint = ApplyHeuristics.getConstraint(c);
		IASTMethod met = null;
		for(IASTMethod m : c.getMethods()){
			if(m.equals(method)){
				met = m;
			}
		}
		if(met != null){
			int startLine =  ((IASTStm) met).getLine();
			int endLine   =  ((IASTStm) met).getLineEnd();
			for(Triplet<String,IASTStm,Class> time : currentClassConstraint){
				int line = time.getValue1().getLine();
				if(startLine <= line && line <= endLine){
					pcfgBuilder.getConstraints().add(time);
				}
			}
		}
		KeyValue<IASTMethod, ASTClass> k = new KeyValue<>(method, c);
		pcfgBuilder.addMethod(k);
		//IndexingFile indexFile = new IndexingFile();
		//indexFile.index(c, reindex);
	}

	public void addClass(ASTClass c){
		for(IASTMethod m : c.getMethods()){
			addClass(c,m);
		}
	}

	public PCFG buildPCFG(){

		PCFG pcfg = pcfgBuilder.convert();

		//annotate with attributes
		List<Pair<String,String>> attrs = new ArrayList<>();
		List<ASTClass> visited = new ArrayList<>();
		ASTClass tmp;
		for(KeyValue<IASTMethod,ASTClass> k : pcfgBuilder.getClasses()){
			tmp = k.getValue();
			if(!visited.contains(tmp)){
				visited.add(tmp);
				for(ASTAttribute a : tmp.getAttributes()){
					attrs.add(new Pair<>(a.getType(), a.getName()));
				}
			}
		}
		pcfg.addAnnotation(String.valueOf(PCFG.DefaultAnnotation.GlobalVars), attrs);

		//set time constraint
		for(Node v : pcfg.getV()){
			for(Triplet<String, IASTStm, Class> c : pcfgBuilder.getConstraints()){
				if(v.equals(c)){
					v.setConstraint(c);
				}
			}
		}

		//optimize
		pcfg.optimize();

		return pcfg;
	}


}
