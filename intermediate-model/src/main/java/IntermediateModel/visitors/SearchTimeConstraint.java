package IntermediateModel.visitors;

import IntermediateModel.structure.ASTClass;
import sun.util.resources.cldr.kk.CalendarData_kk_Cyrl_KZ;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 * The following Class operate over the intermediate model annotating the terms that are time relevant.
 * It expones just one method to the outter world. This method return the IM of a Class annotate with the
 * parts of it that are considered interesting in terms of time constraints.
 *
 */
public class SearchTimeConstraint {

	private boolean isThread;
	public final String _THREAD_CLASS_ = "Thread";
	public final String _THREAD_INTERFACE_ = "Runnable";

	/**
	 * In the future we should pass the type system of the project
	 */
	public SearchTimeConstraint() {
	}

	/**
	 * This method checks:
	 * <ul>
	 *     <li>If it is a Thread</li>
	 *     <li>Attributes that have a time relevant type</li>
	 *     <li>TODO: check imports to collect the time information of that classes</li>
	 * </ul>
	 * @param _class The class to analyze
	 * @return The {@link ASTClass} with its stms are annotated
	 */
	public ASTClass annotateClass(ASTClass _class){
		this.isThread = false;
		//extends thread
		if(_class.getExtendClass().equals(_THREAD_CLASS_)){
			this.isThread = true;
		}
		//implements runnable
		for(String i : _class.getImplmentsInterfaces()){
			if(i.equals(_THREAD_INTERFACE_))
				this.isThread = true;
		}
		if(this.isThread) {
			_class.setTimeCritical(true);
		}
		return _class;
	}
}
