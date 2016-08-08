package IntermediateModelHelper.indexing;

import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexMethod;
import intermediateModel.structure.ASTClass;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class DataTreeType {
	DataTreeType extended = null;
	MongoConnector db;
	IndexData current;

	public DataTreeType(ASTClass _class) {
		MongoOptions options = MongoOptions.getInstance();
		db = MongoConnector.getInstance( options.getDbName() );
		current = db.getIndex(_class).get(0);
		solveBinding();
	}

	public DataTreeType(String _class, String _package) {
		MongoOptions options = MongoOptions.getInstance();
		db = MongoConnector.getInstance( options.getDbName() );
		current = db.getIndex(_class, _package).get(0);
		solveBinding();
	}

	private void solveBinding() {
		String extType = current.getExtendedType();
		if(extType.equals("Object")){
			return;
		}
		for(String i : current.getImports()){
			List<IndexData> imports = db.getFromImport(i);
			for(IndexData index : imports){
				if(index.getClassName().equals(extType)){
					extended = new DataTreeType(index.getName(), index.getClassPackage());
				}
			}
		}
	}

	public IndexData getClassMethod(IndexMethod methodname){
		for(IndexMethod m : current.getListOfMethods()){
			if(m.equalBySignature(methodname)){
				return current;
			}
		}
		if(extended == null){
			return null;
		} else {
			return extended.getClassMethod(methodname);
		}
	}

	public boolean isTypeCompatible(String type){
		if(current.getClassName().equals(type) || current.getExtendedType().equals(type)){
			return true;
		}
		if(extended == null) {
			return false;
		}
		return extended.isTypeCompatible(type);
	}

	public boolean isExtending(String _className){
		if(current.getExtendedType().equals(_className)){
			return true;
		}
		if(extended == null) {
			return false;
		}
		return extended.isExtending(_className);
	}

	public static boolean checkEqualsTypes(String type1, String type2){
		if(type1 == null || type2 == null){
			return false;
		}
		if(!type1.equals(type2)){
			return false;
		}
		return true;
	}
}
