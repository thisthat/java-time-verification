package envirorment;

import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.IASTVar;
import IntermediateModel.structure.ASTAttribute;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTMethod;
import IntermediateModel.structure.ASTVariable;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class BuildEnvirormentClass {
	private Env env;

	private List<String> typeTimeRelevant;// = new ArrayList<>();
	{
		String f = getClass().getClassLoader().getResource("TypeTimeRelevant.txt").getFile();
		try {
			typeTimeRelevant = java.nio.file.Files.readAllLines(Paths.get(f));
		} catch (IOException e) {
			typeTimeRelevant = new ArrayList<>();
		}
	}

	public BuildEnvirormentClass() {
		env = new Env();
	}

	public BuildEnvirormentClass(Env env) {
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
			checkVar(a, env);

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

	/**
	 * Add to the Env a variable if is time related
	 * @param v
	 */
	private void checkVar(IASTVar v, Env env){
		IASTStm w = (IASTStm) v;
		w.setTimeCritical(
				typeTimeRelevant.stream().anyMatch( type -> ( type.equals(v.getType()) ) )
		);
		if(w.isTimeCritical()){
			env.addVar(v);
		}
	}
}
