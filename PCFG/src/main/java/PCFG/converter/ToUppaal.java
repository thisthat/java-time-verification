package PCFG.converter;

import PCFG.structure.CFG;
import PCFG.structure.PCFG;
import PCFG.structure.edge.Edge;
import PCFG.structure.node.Node;
import intermediateModelHelper.heuristic.definition.UndefiniteTimeout;
import uppaal.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * The class convert the PCFG in the DOT language
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ToUppaal implements IConverter {

	boolean hideName = true;
	NTA doc = new NTA();

	/**
	 * Constructor. Initialise the internal variables according to the parameters.
	 */
	public ToUppaal(boolean hideName) {
		this.hideName = hideName;
	}

	public ToUppaal() {
	}

	/**
	 * PrettyPrint the PCFG in the Uppaal syntax
	 * @param pcfg PCFG to convert.
	 * @return	Uppaal representation of the PCFG
	 */
	@Override
	public String convert(PCFG pcfg) {
		for(CFG c : pcfg.getCFG()){
			convert(c);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		doc.setAutoPositioned(true);
		doc.writeXMLWithPrettyLayout(ps);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	private void convert(CFG c) {
		String name = c.getName().substring(c.getName().indexOf("::")+2);
		Automaton aut = new Automaton(name);
		Declaration dec = new Declaration();
		aut.setDeclaration(dec);
		doc.addAutomaton(aut);
		SystemDeclaration sys = new SystemDeclaration();
		sys.addDeclaration(String.format("Process = %s();", name));
		sys.addSystemInstance(name);
		doc.setSystemDeclaration(sys);
		HashMap<Node,Location> map = new HashMap<>();
		for(Node v : c.getV()){
			Location l = new Location(aut, new Name(this.hideName ? "s" + v.getID() : v.getName()), Location.LocationType.COMMITTED, 0, 0);
			if(v.isStart()){
				aut.setInit(l);
			}
			map.put(v, l);
			//time var
			for(String var : v.getResetVars()){
				dec.add(String.format("clock %s;\n",var));
			}
		}
		for(Edge e : c.getE()){
			Transition t = new Transition(aut, map.get(e.getFrom()), map.get(e.getTo()));
			if(e.getFrom().isResetClock()){
				for(String r : e.getFrom().getResetVars()){
					t.addUpdate(String.format("%s = 0", r));
				}
			}
			if(e.getConstraint() != null && !e.getConstraint().isCategory(UndefiniteTimeout.class) && e.getLabel().equals("True")){
				t.setGuard(e.getConstraint().getValue());
			}
		}
	}

}
