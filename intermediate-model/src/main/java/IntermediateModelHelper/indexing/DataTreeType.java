package IntermediateModelHelper.indexing;

import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexMethod;
import intermediateModel.structure.ASTClass;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class DataTreeType {
	DataTreeType extended = null;
	MongoConnector db;
	IndexData current;
	List<DataTreeType> interfaces = new ArrayList<>();

	public DataTreeType(ASTClass _class) {
		MongoOptions options = MongoOptions.getInstance();
		db = MongoConnector.getInstance( options.getDbName() );
		current = db.getIndex(_class).get(0);
		solveBinding();
	}

	public DataTreeType(String _class, String _package) throws Exception {
		MongoOptions options = MongoOptions.getInstance();
		db = MongoConnector.getInstance( options.getDbName());
		current = db.getIndex(_class, _package).get(0);
		solveBinding();
	}

	private void solveBinding() {
		String extType = current.getExtendedType();
		/* if(extType.equals("Object")){
			return;
		} */
		for(String i : current.getImports()){
			List<IndexData> imports = db.getFromImport(i);
			for(IndexData index : imports){
				//Check on types
				if(index.getClassName().equals(extType)){
					try {
						extended = new DataTreeType(index.getName(), index.getClassPackage());
					} catch (Exception e){
						//we cannot resolve the binding for the extended
						extended = null;
					}

				}
				//Check on interfaces
				for(String intf : index.getInterfacesImplemented()){
					if( index.getInterfacesImplemented().stream().anyMatch( tInterface -> tInterface.equals(intf)) ){
						try {
							DataTreeType tmp = new DataTreeType(intf, index.getClassPackage());
							interfaces.add(tmp);
						} catch (Exception e){
							//we cannot resolve the binding for the interface
						}
					}
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
		if(current.getClassName().equals("IXString_v2")){
			int i = 0;
			int j = i + 1;
		}
		if(current.getClassName().equals(type) || current.getExtendedType().equals(type)){ //extended or current type
			return true;
		}
		//interfaces
		boolean flag = false;
		for(DataTreeType _interface : interfaces){
			if(_interface.isTypeCompatible(type)){
				flag = true;
			}
		}
		if(flag){
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

	public static boolean checkCompatibleTypes(String type1, String type2, String pkg1, String pkg2){
		if(type1 == null || type2 == null){
			return false;
		}
		//some one extends the other?
		try {
			DataTreeType t1 = new DataTreeType(type1, pkg1);
			if (t1.isTypeCompatible(type2)) {
				return true;
			}
		} catch (Exception e){
			//we don't have to do anything
		}
		if(type1.equals(type2)){
			return true;
		}
		return false;
	}

	public static boolean checkEqualsTypes(String type1, String type2, String pkg1, String pkg2){
		if(type1 == null || type2 == null){
			return false;
		}
		//some one extends the other?
		try {
			DataTreeType t1 = new DataTreeType(type1, pkg1);
			if (t1.isTypeCompatible(type2)) {
				return true;
			}
		} catch (Exception e){
			//we don't have to do anything
			//System.err.println(e.getMessage());
		}
		try{
			DataTreeType t2 = new DataTreeType(type2, pkg2);
			if (t2.isTypeCompatible(type1)) {
				return true;
			}
		} catch (Exception e){
			//we don't have to do anything
			//System.err.println(e.getMessage());
		}
		if(type1.equals(type2)){
			return true;
		}
		return false;
	}
}
