package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTFor extends IASTStm implements IASTHasStms {
	List<IASTStm> stms;
	List<ASTRE> init;
	ASTRE expr;
	List<ASTRE> post;

	public ASTFor(Token start, Token end) {
		super(start, end);
		this.init = new ArrayList<ASTRE>();
		this.post = new ArrayList<ASTRE>();
		this.stms = new ArrayList<IASTStm>();
	}

	public ASTFor(Token start, Token end, List<ASTRE> init, ASTRE expr, List<ASTRE> post) {
		super(start, end);
		this.init = init;
		this.expr = expr;
		this.post = post;
		this.stms = new ArrayList<IASTStm>();
	}

	public ASTFor(Token start, Token end, ASTRE init, ASTRE expr, ASTRE post) {
		super(start, end);
		this.init = new ArrayList<ASTRE>();
		this.post = new ArrayList<ASTRE>();
		this.stms = new ArrayList<IASTStm>();
		this.init.add(init);
		this.expr = expr;
		this.post.add(post);
	}

	public List<IASTStm> getStms() {
		return stms;
	}

	public void setStms(List<IASTStm> stms) {
		this.stms = stms;
	}

	public List<ASTRE> getInit() {
		return init;
	}

	public void setInit(List<ASTRE> init) {
		this.init = init;
	}
	public void setInit(ASTRE init) {
		this.init.add(init);
	}

	public ASTRE getExpr() {
		return expr;
	}

	public void setExpr(ASTRE expr) {
		this.expr = expr;
	}

	public List<ASTRE> getPost() {
		return post;
	}

	public void setPost(List<ASTRE> post) {
		this.post = post;
	}
	public void setPost(ASTRE post) {
		this.post.add(post);
	}

	@Override
	public void addStms(IASTStm stm) {
		stms.add(stm);
	}

	@Override
	public String toString() {
		String out = "for(";
		if(init.size() > 0)
			out += init.get(0).toString();
		if(expr != null)
			out += ";" + expr.toString();
		if(post.size() > 0)
			out += ";" + post.get(0).toString();
		out += ")\n";
		for(IASTStm e : stms){
			out += e.toString() + "\n";
		}
		out += "\nendFor";
		return out;
	}
}
