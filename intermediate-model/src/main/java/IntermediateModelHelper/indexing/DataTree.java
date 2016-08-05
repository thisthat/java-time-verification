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
public class DataTree {
	DataTree extended = null;
	MongoConnector db;
	IndexData current;

	public DataTree(ASTClass _class) {
		MongoOptions options = MongoOptions.getInstance();
		db = MongoConnector.getInstance( options.getDbName() );
		current = db.getIndex(_class).get(0);
		solveBinding();
	}

	public DataTree(String _class, String _package) {
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
					extended = new DataTree(index.getName(), index.getClassPackage());
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
}
