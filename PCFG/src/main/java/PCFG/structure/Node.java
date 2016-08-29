package PCFG.structure;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Node implements INode {
	String name;
	String code;
	TYPE type;
	int id = 0;

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

	public Node(String name, String code, TYPE type) {
		this.name = name;
		this.code = code;
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
}
