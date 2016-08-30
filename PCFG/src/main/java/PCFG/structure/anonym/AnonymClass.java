package PCFG.structure.anonym;

import PCFG.structure.CFG;
import PCFG.structure.node.INode;

import java.util.ArrayList;
import java.util.List;

/**
 * With this class we represent the PCFG for an anonymous class.
 * We have a CFG for each method.
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class AnonymClass implements INode {

	private static int ID = 0;
	private int id;
	private List<CFG> methods = new ArrayList<>();

	public AnonymClass() {
		this.id = ID++;
	}

	public void addMethods(CFG method) {
		this.methods.add(method);
	}

	@Override
	public String getName() {
		return "anonymousClass_" + this.id;
	}

	@Override
	public int getID() {
		return this.id;
	}

	@Override
	public String toGraphViz(boolean hideName) {
		return null;
	}
}
