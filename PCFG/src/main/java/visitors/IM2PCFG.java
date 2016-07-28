package visitors;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ParseIM;
import structure.PCFG;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IM2PCFG extends ParseIM {
	List<ASTClass> classes = new ArrayList<>();

	public IM2PCFG(List<ASTClass> classes) {
		this.classes = classes;
	}

	public IM2PCFG() {
	}

	public void addClass(ASTClass c){
		this.classes.add(c);
	}

	public PCFG buildPCFG(){
		PCFG pcfg = new PCFG();

		return pcfg;
	}


}
