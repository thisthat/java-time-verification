package PCFG.creation;

import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModelHelper.indexing.IndexingSyncBlock;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import PCFG.creation.helper.CalculateSyncBlock;
import PCFG.creation.helper.CalculateSyncCall;
import PCFG.creation.helper.CreateCFG;
import PCFG.structure.PCFG;
import PCFG.structure.node.Node;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import org.javatuples.KeyValue;
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
public class IM2PCFG {

	private List<KeyValue<KeyValue<IASTMethod,ASTClass>, List<IndexSyncBlock> >> indexs = new ArrayList<>();
	CreateCFG pcfgBuilder = new CreateCFG();

	public IM2PCFG(List<KeyValue<IASTMethod,ASTClass>> classes) {
		for(KeyValue<IASTMethod,ASTClass> k : classes){
			pcfgBuilder.addMethod(k);
		}
	}

	public IM2PCFG() {

	}

	public int getConstraintsSize() {
		return pcfgBuilder.getConstraints().size();
	}

	boolean forceReindex = true;

	public boolean isForceReindex() {
		return forceReindex;
	}

	public void setForceReindex(boolean forceReindex) {
		this.forceReindex = forceReindex;
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

	public PCFG buildPCFG(){
		//init data
		this.indexs.clear();
		List<KeyValue<IASTMethod,ASTClass>> classes = pcfgBuilder.getClasses();
		for(KeyValue<IASTMethod,ASTClass> k : classes){
			IndexingFile indexCall = new IndexingFile();
			indexCall.index(k.getValue(), this.forceReindex);
			IndexingSyncBlock indexing = new IndexingSyncBlock();
			List<IndexSyncBlock> blocks = indexing.index(k.getValue(), this.forceReindex);
			List<IndexSyncBlock> toAdd = new ArrayList<>();
			IASTMethod m = k.getKey();
			for(IndexSyncBlock b : blocks){
				boolean f = false;
				if(m.getName().equals(b.getMethodName()) && m.getParameters().size() == b.getSignature().size()){
					f = true;
					for(int i = 0; i < m.getParameters().size(); i++){
						if(!m.getParameters().get(i).getType().equals( b.getSignature().get(i) )){
							f = false;
						}
					}
				}
				if(m.getStart() > b.getStart() || b.getEnd() > m.getEnd() ){
					f = false;
				}
				if(f){
					toAdd.add(b);
				}
			}
			this.indexs.add( new KeyValue<>(k, toAdd) );
		}

		PCFG pcfg = pcfgBuilder.convert();

		//check for sync blocks
		CalculateSyncBlock.calculateSyncBlock(classes, this.indexs, pcfg);

		//check for call on sync methods
		CalculateSyncCall.calculateSyncCall(classes, pcfg);

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
