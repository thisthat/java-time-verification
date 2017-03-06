package smellDetector;

import intermediateModelHelper.indexing.structure.IndexParameter;
import intermediateModelHelper.indexing.structure.IndexSyncBlock;
import intermediateModelHelper.types.DataTreeType;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CheckSmell {


	public static boolean isSmell(IndexSyncBlock s1, IndexSyncBlock s2){
		boolean isSameClass = sameClass(s1,s2);
		IndexParameter varInner = s1.getSyncVar(); //inner.getEnv().getVar(inner.getExpr());
		if(varInner == null && isSameClass){
			if(s1.getExpr().equals(s2.getExpr())){
				//we have a match on this or ClassName.class
				return true;
			}
		} else if(varInner != null) {
			boolean canWeCheck = 	(!isSameClass) || //it is not the same class
					//(s1.isInherited() && s2.isInherited() && s1.sameInheritance(s2)) || //both vars can be readed from outside
					(s1.isAccessibleWritingFromOutside() && s2.getExpr().equals("this")) || //the first is accessible writing from outside and the other is this
					(s2.isAccessibleWritingFromOutside() && s1.getExpr().equals("this")) || //the outter is accessible writing from outside and the inner is this
					(s2.isAccessibleWritingFromOutside() && s1.isAccessibleWritingFromOutside()) ; //both can share a variable from outside
			if(
					canWeCheck //both are accessible from outside or we are checking on same classes
						&&
					DataTreeType.checkEqualsTypes(s1.getExprType(), s2.getExprType(), s1.getExprPkg(), s2.getExprPkg())
			){
				return true;
			}
		}
		return false;
	}


	private static boolean sameClass(IndexSyncBlock s1, IndexSyncBlock s2) {
		return s1.getPackageName().equals(s2.getPackageName()) && s1.getName().equals(s2.getName());
	}

}
