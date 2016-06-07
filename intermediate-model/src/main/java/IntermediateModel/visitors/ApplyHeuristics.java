package IntermediateModel.visitors;

import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.IASTVar;
import IntermediateModel.structure.ASTAttribute;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTMethod;
import IntermediateModel.structure.ASTVariable;
import envirorment.Env;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 * The following Class operate over the intermediate model annotating the terms that are time relevant.
 * It expones just one method to the outter world. This method return the IM of a Class annotate with the
 * parts of it that are considered interesting in terms of time constraints.
 *
 */
public class ApplyHeuristics {

	private boolean isThread;
	public final String _THREAD_CLASS_ = "Thread";
	public final String _THREAD_INTERFACE_ = "Runnable";
	private List<String> typeTimeRelevant;// = new ArrayList<>();
	private Env env;
	{
		String f = getClass().getClassLoader().getResource("TypeTimeRelevant.txt").getFile();
		try {
			typeTimeRelevant = java.nio.file.Files.readAllLines(Paths.get(f));
		} catch (IOException e) {
			typeTimeRelevant = new ArrayList<>();
		}
	}

	/**
	 * In the future we should pass the types of the project that are annotated manually
	 */
	public ApplyHeuristics() {
		env = new Env();
	}

	public ApplyHeuristics(Env env) {
		this.env = env;
	}


	/**
	 * This method add to the envirorment:
	 * <ul>
	 *     <li>Attributes that have a time relevant type</li>
	 *     <li>TODO: check imports to collect the time information of that classes</li>
	 * </ul>
	 * @param _class The class to analyze
	 * @return The {@link Env} fulfill with the Info for parsing
	 */

	private void buildEnvClass(ASTClass _class, Env env){
		//check over attributes
		for(ASTAttribute a : _class.getAttributes()){
			a.setTimeCritical(
					typeTimeRelevant.stream().anyMatch( type -> ( type.equals(a.getType()) ) )
			);
			if(a.isTimeCritical()){
				env.addVar(a);
			}
		}
		//check over methods
		for(IASTMethod m : _class.getMethods()){
			buildEnvMethod(m);
		}
	}

	/**
	 * This method checks for all the stms in a method:
	 * <ul>
	 *     <li>Return of time relevant type</li>
	 *     <li>Return of time relevant type</li>
	 * </ul>
	 * @param m Method to annotate
	 */
	private void buildEnvMethod(IASTMethod mm){
		//return type is one of the interesting one - only methods
		if(mm instanceof ASTMethod){
			ASTMethod m = (ASTMethod) mm;
			String retType = m.getReturnType();
			m.setTimeCritical(
					typeTimeRelevant.stream().anyMatch( type -> ( type.equals(retType) ) )
			);
			if(m.isTimeCritical()){
				env.addMethod( m , new Env());
			}
		}
	}

}
