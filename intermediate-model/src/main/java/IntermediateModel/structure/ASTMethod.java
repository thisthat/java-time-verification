package intermediateModel.structure;

import IntermediateModelHelper.types.DataTreeType;
import intermediateModel.interfaces.*;
import org.antlr.v4.runtime.Token;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class ASTMethod extends IASTStm implements IASTMethod, IASTHasStms, IASTVisitor {
	String name;
	String returnType;
	List<ASTVariable> parameters;
	List<String> exceptionsThrowed;
	List<IASTStm> stms = new ArrayList<>();
	boolean isSyncronized = false;


	public ASTMethod(Token start, Token end, String name, String returnType, List<ASTVariable> parameters, List<String> exceptionsThrowed, boolean isSyncronized) {
		super(start,end);
		this.name = name;
		this.returnType = returnType;
		this.parameters = parameters;
		this.exceptionsThrowed = exceptionsThrowed;
		this.isSyncronized = isSyncronized;
	}

	public ASTMethod(int start, int end, String name, String returnType, List<ASTVariable> parameters, List<String> exceptionsThrowed, boolean isSyncronized) {
		super(start,end);
		this.name = name;
		this.returnType = returnType;
		this.parameters = parameters;
		this.exceptionsThrowed = exceptionsThrowed;
		this.isSyncronized = isSyncronized;
	}

	public boolean isSyncronized() {
		return isSyncronized;
	}

	public void setSyncronized(boolean syncronized) {
		isSyncronized = syncronized;
	}

	public List<String> getExceptionsThrowed() {
		return exceptionsThrowed;
	}

	public void setExceptionsThrowed(List<String> exceptionsThrowed) {
		this.exceptionsThrowed = exceptionsThrowed;
	}

	public void addThrows(String eType){
		exceptionsThrowed.add(eType);
	}

	public String getName() {
		return name;
	}

	public String getReturnType() {
		return returnType;
	}

	public List<ASTVariable> getParameters() {
		return parameters;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTMethod)) return false;
		ASTMethod astMethod = (ASTMethod) o;
		if (getName() != null ? !getName().equals(astMethod.getName()) : astMethod.getName() != null) return false;
		if (getReturnType() != null ? !getReturnType().equals(astMethod.getReturnType()) : astMethod.getReturnType() != null)
			return false;
		if (getParameters() != null ? !getParameters().equals(astMethod.getParameters()) : astMethod.getParameters() != null)
			return false;
		if (getStms() != null ? !getStms().equals(astMethod.getStms()) : astMethod.getStms() != null)
			return false;

		return true;
	}

	public String toString(){
		String out;
		out = "\t" + name + "(";

		for(ASTVariable v: parameters){
			out += v.toString() + ",";
		}
		if(parameters.size() > 0){
			out = out.substring(0,out.length()-1);
		}
		out += ") : " + returnType + " ";
		return out;
	}

	public List<String> getSignature(){
		List<String> out = new ArrayList<>();
		for(ASTVariable p : parameters){
			out.add(p.getType());
		}
		return out;
	}

	public void setStms(List<IASTStm> stms) {
		this.stms = stms;
	}
	public void addStms(IASTStm stm) {
		this.stms.add(stm);
	}
	@Override
	public List<IASTStm> getStms() {
		return stms;
	}


	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTMethod(this);
		for(ASTVariable p : parameters){
			p.visit(visitor);
		}
		for(IASTStm s : stms){
			s.visit(visitor);
		}
		visitor.exitASTMethod(this);
	}

	public boolean equalsBySignature(IASTMethod c) {
		if(!c.getName().equals(this.name)) return false;
		if(c.getParameters().size() != this.parameters.size()) return false;
		boolean flag = true;
		List<ASTVariable> pars = c.getParameters();
		for(int i = 0; i < this.parameters.size(); i++){
			if(!pars.get(i).getType().equals(this.parameters.get(i).getType())){
				flag = false;
			}
		}
		return flag;
	}

	@Override
	public boolean equalsBySignature(String name, List<Pair<String, String>> signature) {
		if(name.equals(this.name)) return false;
		if(signature.size() != this.parameters.size()) return false;
		boolean flag = true;
		for(int i = 0; i < this.parameters.size(); i++){
			if(!signature.get(i).getValue1().equals(this.parameters.get(i).getType())){
				flag = false;
			}
		}
		return flag;
	}
}
