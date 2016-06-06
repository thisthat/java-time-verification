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
public class BuildClassEnvirorment {

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
	 * In the future we should pass the type system of the project
	 */
	public BuildClassEnvirorment() {
		env = new Env();
	}




}
