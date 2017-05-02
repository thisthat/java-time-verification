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

	public enum NAMING {
		LINE,
		ID,
		PRETTY
	}
	NAMING typeName = NAMING.LINE;

	NTA doc = new NTA();

	/**
	 * Constructor. Initialise the internal variables according to the parameters.
	 */
	public ToUppaal(NAMING typeName) {
		this.typeName = typeName;
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
			String nameLoc;
			switch(typeName){
				case LINE:
					switch(v.getType()){
						case TRY:
						case BREAK:
						case THROW:
						case NORMAL:
						case RETURN:
						case SWITCH:
						case FOREACH:
						case IF_EXPR:
						case CONTINUE:
						case WHILE_EXPR:
							nameLoc = "l" + v.getLine();
							break;
						default:
							nameLoc = "l" + v.getLine() +"_"+ v.getNameNoID();
						 	break;
					}
					break;
				case PRETTY: nameLoc = v.getName(); break;
				default: nameLoc = "s" + v.getID(); break;
			}
			Location l = new Location(aut, new Name(nameLoc), Location.LocationType.COMMITTED, 0, 0);
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
			if(e.getConstraint() != null && !e.getConstraint().isCategory(UndefiniteTimeout.class)){
				//we have a cnst to represent, but if we are in an if branch, only the true branch will have it
				String l = e.getLabel();
				if(l.equals("") || l.equals("True")){
					t.setGuard(e.getConstraint().getValue());
				}
			}
			/*if(e.getConstraint() != null && !e.getConstraint().isCategory(UndefiniteTimeout.class) && e.getLabel() != null && e.getLabel().equals("True")){
				t.setGuard(e.getConstraint().getValue());
			}*/
		}
	}

}
