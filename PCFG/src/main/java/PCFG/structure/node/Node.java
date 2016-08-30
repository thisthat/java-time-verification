package PCFG.structure.node;

import PCFG.structure.PCFG;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Node implements INode {
	String name;
	String code;
	TYPE type;
	int id = 0;
	int start,end;

	public static int _ID = 0;

	public enum TYPE {
		RETURN,
		BREAK,
		CONTINUE,
		FOREACH,
		THROW,
		TRY,
		FINALLY,
		SWITCH,
		NORMAL,
		USELESS
	}

	public Node(String name, String code, TYPE type, int start, int end) {
		this.name = name;
		this.code = code;
		this.type = type;
		this.id = _ID++;
		this.start = start;
		this.end = end;
	}
	public Node(String name, String code, TYPE type) {
		this.name = name;
		this.code = code;
		if(type == TYPE.NORMAL){
			//TODO
			//we should use breakpoint on this line to check which are the call to node that does not send start and end position in the file
			StackTraceElement[] cause = Thread.currentThread().getStackTrace();
			//System.err.println(Arrays.toString(cause));
		}
		this.type = type;
		this.id = _ID++;
	}

	public String getName() {
		return name + "_" + this.id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public int getID() {
		return id;
	}

	public boolean equalsByCode(Node obj) {
		return obj.getCode().equals(this.code);
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	@Override
	public String toGraphViz(boolean hideName) {
		String name;
		if(hideName) {
			name = "s" + this.getID();
		} else {
			name = this.getName();
		}
		return "\t" + name;
	}
}
