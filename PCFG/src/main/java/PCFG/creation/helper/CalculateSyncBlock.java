package PCFG.creation.helper;

import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexParameter;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import PCFG.structure.PCFG;
import PCFG.structure.edge.SyncEdge;
import PCFG.structure.node.SyncNode;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import org.javatuples.KeyValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CalculateSyncBlock {

	public static void calculateSyncBlock(List<KeyValue<IASTMethod,ASTClass>> classes, List<KeyValue<KeyValue<IASTMethod,ASTClass>,IndexData>> indexs, PCFG pcfg){
		calculateSyncBlockDifferentClasses(classes, indexs, pcfg);
	}

	private static void calculateSyncBlockDifferentClasses(List<KeyValue<IASTMethod,ASTClass>> classes, List<KeyValue<KeyValue<IASTMethod,ASTClass>,IndexData>> indexes, PCFG pcfg) {
		//get the sync blocks of the methods that we are looking for
		List<IndexSyncBlock> syncBlocks = getSyncBlocks(classes, indexes);

		//everyone is checked against everyone
		for(IndexSyncBlock outter : syncBlocks){
			IndexParameter varOutter = outter.getEnv().getVar(outter.getExpr());
			if(varOutter == null) { //could be a method call or something else than a simple var :(
				//for the moment we consider only variables
				continue;
			}
			for(IndexSyncBlock inner : syncBlocks) {
				if (outter == inner) { //we allow to check two times the same method for thread reasons
						// ||(outter.getClassName().equals(inner.getClassName()) && outter.getPackageName().equals(inner.getPackageName()) && outter.getMethodName().equals(inner.getMethodName())) ) {
					//we do not check myself with myself
					continue;
				}
				IndexParameter varInner = inner.getEnv().getVar(inner.getExpr());
				if(varInner == null){//could be a method call or something else than a simple var :(
					//for the moment we consider only variables
					continue;
				}
				if(varInner.getType().equals(varOutter.getType())){
					//we gotta a match
					SyncNode outSync = pcfg.getSyncNodeByExpr( outter.getExpr(), outter.getStart(), outter.getEnd(), outter.getLine() );
					SyncNode inSync  = pcfg.getSyncNodeByExpr( inner.getExpr(), inner.getStart(), inner.getEnd(), inner.getLine() );
					if(outSync == inSync){ //the case where we have same class 2+ times
						inSync  = pcfg.getSyncNodeByExprSkip( inner.getExpr(), inner.getStart(), inner.getEnd(), inner.getLine(), 1 );
					}
					SyncEdge sEdge = null;
					if(outSync == null || inSync == null){
						// we have smt in the hidden class -> How to handle?
						System.err.println("Null pointer to sync block");
						for(KeyValue<IASTMethod,ASTClass> c : classes){
							System.err.println(c.getKey()  + " :: " + c.getValue().getPackageName() + "." + c.getValue().getName());
						}
						System.err.println("_____");
					} else {
						//link between two different sync blocks
						try {
							sEdge = new SyncEdge(outSync, inSync);
							pcfg.addSyncEdge(sEdge);
						} catch (SyncEdge.MalformedSyncEdge malformedSyncEdge) {
							malformedSyncEdge.printStackTrace();
						}
					}
				}
			}
		}
	}


	private static List<IndexSyncBlock> getSyncBlocks(List<KeyValue<IASTMethod,ASTClass>> classes, List<KeyValue<KeyValue<IASTMethod,ASTClass>, IndexData>> indexes){
		List<IndexSyncBlock> syncBlocks = new ArrayList<>();
		for(KeyValue<IASTMethod,ASTClass> c : classes){
			IndexData data = null;
			for(KeyValue<KeyValue<IASTMethod,ASTClass>, IndexData> k : indexes) {
				if(k.getKey().equals(c)){
					data = k.getValue();
				}
			}
			for(IndexSyncBlock s : data.getListOfSyncBlocks()){
				if(
						s.getMethodName().equals(
								c.getKey().getName())
						)
				{
					syncBlocks.add( new IndexSyncBlock(s) );
				}
			}
		}
		return (syncBlocks);
	}

	private static List<IndexSyncBlock> removeDuplicates(List<IndexSyncBlock> tmp_syncBlocks){
		List<IndexSyncBlock> syncBlocks = new ArrayList<>();
		for(IndexSyncBlock is : tmp_syncBlocks){
			boolean exists = false;
			for(IndexSyncBlock actualSync : syncBlocks){
				if(actualSync.equals(is)){
					exists = true;
				}
			}
			if(!exists){
				syncBlocks.add(is);
			}
		}
		return syncBlocks;
	}

	private static void calculateSyncBlockSameClasses(List<KeyValue<IASTMethod,ASTClass>> classes, List<KeyValue<KeyValue<IASTMethod,ASTClass>,IndexData>> indexs, PCFG pcfg) {
		//get the sync blocks of the methods that we are looking for
		List<IndexSyncBlock> tmp_syncBlocks = new ArrayList<>();
		List<IndexSyncBlock> syncBlocks = new ArrayList<>();
		for(KeyValue<IASTMethod,ASTClass> c : classes){
			IndexData data = null;
			for(KeyValue<KeyValue<IASTMethod,ASTClass>, IndexData> k : indexs) {
				if(k.getKey().equals(c)){
					data = k.getValue();
				}
			}
			for(IndexSyncBlock s : data.getListOfSyncBlocks()){
				if(
						s.getMethodName().equals(
								c.getKey().getName())
						)
				{
					tmp_syncBlocks.add(s);
				}
			}
		}
		//remove duplicate -> it happens when we are using the same class to do the job
		for(IndexSyncBlock is : tmp_syncBlocks){
			boolean exists = false;
			for(IndexSyncBlock actualSync : syncBlocks){
				if(actualSync.equals(is)){
					exists = true;
				}
			}
			if(!exists){
				syncBlocks.add(is);
			}
		}
		//everyone is checked against everyone
		for(IndexSyncBlock outter : syncBlocks){
			IndexParameter varOutter = outter.getEnv().getVar(outter.getExpr());
			if(varOutter == null) { //could be a method call or something else than a simple var :(
				//for the moment we consider only variables
				continue;
			}
			for(IndexSyncBlock inner : syncBlocks) {
				if (outter == inner ||
						(outter.getClassName().equals(inner.getClassName()) && outter.getPackageName().equals(inner.getPackageName()) && outter.getMethodName().equals(inner.getMethodName())) ) {
					//we do not check myself with myself
					continue;
				}
				IndexParameter varInner = inner.getEnv().getVar(inner.getExpr());
				if(varInner == null){//could be a method call or something else than a simple var :(
					//for the moment we consider only variables
					continue;
				}
				if(varInner.getType().equals(varOutter.getType())){
					//we gotta a match
					SyncNode outSync = pcfg.getSyncNodeByExpr( outter.getExpr(), outter.getStart(), outter.getEnd(), outter.getLine() );
					SyncNode inSync  = pcfg.getSyncNodeByExpr( inner.getExpr(), inner.getStart(), inner.getEnd(), inner.getLine() );
					SyncEdge sEdge = null;
					if(outSync == null || inSync == null){
						// we have smt in the hidden class -> How to handle?
						System.err.println("Null pointer to sync block");
						for(KeyValue<IASTMethod,ASTClass> c : classes){
							System.err.println(c.getKey()  + " :: " + c.getValue().getPackageName() + "." + c.getValue().getName());
						}
						System.err.println("_____");
					} else {
						//link between two different sync blocks
						try {
							sEdge = new SyncEdge(outSync, inSync);
							pcfg.addSyncEdge(sEdge);
						} catch (SyncEdge.MalformedSyncEdge malformedSyncEdge) {
							malformedSyncEdge.printStackTrace();
						}
					}
				}
			}
		}
	}
}
