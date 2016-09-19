package PCFG.creation.helper;

import IntermediateModelHelper.indexing.GenerateMethodSyncCallList;
import IntermediateModelHelper.indexing.structure.SyncMethodCall;
import PCFG.structure.*;
import PCFG.structure.edge.SyncEdge;
import PCFG.structure.node.Node;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTConstructor;
import intermediateModel.structure.ASTMethod;
import intermediateModel.structure.ASTRE;
import intermediateModel.visitors.DefaultASTVisitor;
import org.javatuples.KeyValue;

import java.util.HashMap;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CalculateSyncCall {

	public static void calculateSyncCall(List<KeyValue<IASTMethod,ASTClass>> classes, PCFG pcfg) {
		HashMap<KeyValue<IASTMethod,ASTClass>,List<SyncMethodCall>> syncCalls = new HashMap<>();

		for(KeyValue<IASTMethod,ASTClass> c : classes){
			//consider also hidden methods
			c.getValue().visit(new DefaultASTVisitor(){
				@Override
				public void enterASTMethod(ASTMethod elm) {
					if(elm.equalsBySignature(c.getKey())) {
						ASTClass _class = c.getValue();
						GenerateMethodSyncCallList helper = new GenerateMethodSyncCallList(_class, elm);
						syncCalls.put(c, helper.calculateSyncCallList());
					}
				}

				@Override
				public void enterASTConstructor(ASTConstructor elm) {
					if(elm.equalsBySignature(c.getKey())) {
						ASTClass _class = c.getValue();
						GenerateMethodSyncCallList helper = new GenerateMethodSyncCallList(_class, elm);
						syncCalls.put(c, helper.calculateSyncCallList());
					}
				}
			});
		}

		//we have the list of all -> create the link between 'em
		for(KeyValue<IASTMethod,ASTClass> cOut : classes){
			for(KeyValue<IASTMethod,ASTClass> cIn : classes){
				if(cIn.equals(cOut)) continue;
				List<SyncMethodCall> outter = syncCalls.get(cOut);
				List<SyncMethodCall> inner  = syncCalls.get(cIn);
				for(SyncMethodCall outMethod : outter){
					for(SyncMethodCall inMethod : inner){
						if(outMethod.equalsBySignature(inMethod)){
							createLink(outMethod, inMethod, pcfg, classes);
						}
					}
				}
			}
		}
	}

	private static void createLink(SyncMethodCall outMethod, SyncMethodCall inMethod, PCFG pcfg, List<KeyValue<IASTMethod,ASTClass>> classes) {
		Node from = null;
		Node to = null;
		ASTRE codeOut = outMethod.getNode();
		ASTRE codeIn  = inMethod.getNode();
		for(Node v : pcfg.getV()){
			if(v.equals(codeOut)){
				from = v;
			} else if(v.equals(codeIn)){
				to = v;
			}
		}
		try {
			SyncEdge e = new SyncEdge(from, to);
			pcfg.addSyncEdge(e);
		} catch (SyncEdge.MalformedSyncEdge malformedSyncEdge) {
			malformedSyncEdge.printStackTrace();
		} catch (Exception e) {
			System.out.println("error creating link: ");
			for(KeyValue<IASTMethod,ASTClass> c : classes){
				System.out.println("\t" + c.getValue().toString() + " :: " + c.getKey());
			}
		}
	}
}
