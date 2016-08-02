package PCFG.visitors.helper;

import intermediateModel.structure.ASTRE;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class SyncMethodCall {
	private String _packageName;
	private String _className;
	private String _methodName;
	private ASTRE node;

	public SyncMethodCall(String _packageName, String _className, String _methodName, ASTRE node) {
		this._packageName = _packageName;
		this._className = _className;
		this._methodName = _methodName;
		this.node = node;
	}

	public String get_packageName() {
		return _packageName;
	}

	public String get_className() {
		return _className;
	}

	public String get_methodName() {
		return _methodName;
	}

	@Override
	public String toString() {
		return _packageName + "." + _className + "#" + _methodName;
	}

	public ASTRE getNode() {
		return node;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SyncMethodCall)) return false;

		SyncMethodCall that = (SyncMethodCall) o;

		if (get_packageName() != null ? !get_packageName().equals(that.get_packageName()) : that.get_packageName() != null)
			return false;
		if (get_className() != null ? !get_className().equals(that.get_className()) : that.get_className() != null)
			return false;
		return get_methodName() != null ? get_methodName().equals(that.get_methodName()) : that.get_methodName() == null;

	}
}
