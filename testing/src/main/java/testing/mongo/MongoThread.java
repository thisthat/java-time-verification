package testing.mongo;

import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.structure.IndexData;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class MongoThread {
	public static void main(String[] args){
		MongoConnector mongo = MongoConnector.getInstance("vuze_eval");
		List<IndexData> datas = mongo.getClassesThatImports("com.aelitis.azureus.core.content.RelatedContentManager","RCMSearchXFer");
		System.out.println(datas.size());
	}

}
