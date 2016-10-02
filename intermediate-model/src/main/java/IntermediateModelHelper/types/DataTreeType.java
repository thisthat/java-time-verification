package IntermediateModelHelper.types;

import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexMethod;
import intermediateModel.structure.ASTClass;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class DataTreeType {
	DataTreeType extended = null;
	IndexData current;
	List<DataTreeType> interfaces = new ArrayList<>();

	private static Map<Pair<String,String>,Triplet<DataTreeType,IndexData,List<DataTreeType>>> cache = new HashMap<>();

	public DataTreeType(ASTClass _class) {
		MongoOptions options = MongoOptions.getInstance();
		MongoConnector db = MongoConnector.getInstance( options.getDbName() );
		Pair<String,String> p = new Pair<>(_class.getPackageName(), _class.getName());
		if(cache.containsKey(p)){
			Triplet<DataTreeType,IndexData,List<DataTreeType>> t = cache.get(p);
			extended = t.getValue0();
			current = t.getValue1();
			interfaces = t.getValue2();
		} else {
			current = db.getIndex(_class).get(0);
			solveBinding(db);
			Triplet<DataTreeType,IndexData,List<DataTreeType>> t = new Triplet<>(extended, current, interfaces);
			cache.put(p, t);
		}

	}

	public DataTreeType(String _class, String _package) {
		MongoOptions options = MongoOptions.getInstance();
		MongoConnector db = MongoConnector.getInstance( options.getDbName());
		Pair<String,String> p = new Pair<>(_package, _class);
		if(cache.containsKey(p)){
			Triplet<DataTreeType,IndexData,List<DataTreeType>> t = cache.get(p);
			extended = t.getValue0();
			current = t.getValue1();
			interfaces = t.getValue2();
		} else {
			current = db.getIndex(_class, _package).get(0);
			solveBinding(db);
			Triplet<DataTreeType,IndexData,List<DataTreeType>> t = new Triplet<>(extended, current, interfaces);
			cache.put(p, t);
		}
	}

	private void solveBinding(MongoConnector db) {
		String extType = current.getExtendedType();
		/* if(extType.equals("Object")){
			return;
		} */
		for(String i : current.getImports()){
			List<IndexData> imports = db.getFromImport(i, false);
			for(IndexData index : imports){
				//Check on types to resolve the extends
				if(index.getClassName().equals(extType)){
					try {
						extended = new DataTreeType(index.getName(), index.getClassPackage());
					} catch (Exception e){
						//we cannot resolve the binding for the extended
						extended = null;
					}
				}
				//Check on interfaces -> ERROR HERE
				for(String intf : index.getInterfacesImplemented()){
					if( current.getInterfacesImplemented().stream().anyMatch( tInterface -> tInterface.equals(intf)) ){
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


	public boolean isTypeCompatible(String type){
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
		//null is always compatible
		if(type1.equals("null")) return true;
		if(type2.equals("null")) return true;
		//this is ok only when there there are objects
		if(type1.equals("this") && Character.isUpperCase(type2.charAt(0))) return true;
		if(type2.equals("this") && Character.isUpperCase(type1.charAt(0))) return true;
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
		return checkEqualsTypes(type1,type2);
	}

	private static boolean checkEqualsTypes(String type1, String type2) {
		//search for basic types
		List<String> basic_int = new ArrayList<>(Arrays.asList(new String[]{"short", "int", "long", "float", "double", "Short", "Integer", "Long", "Float", "Double"}));
		List<String> basic_bool = new ArrayList<>(Arrays.asList(new String[]{"boolean","Boolean"}));
		if(basic_int.contains(type1) && basic_int.contains(type2)) return true;
		if(basic_bool.contains(type1) && basic_bool.contains(type2)) return true;
		return type1.equals(type2);
	}

}
