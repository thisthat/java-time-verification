package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;
import org.neo4j.ogm.annotation.NodeEntity;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class ASTMethod extends IASTStm implements IASTMethod, IASTHasStms {
	String name;
	String returnType;
	List<ASTVariable> parameters;
	List<String> exceptionsThrowed;
	List<IASTStm> stms = new ArrayList<>();


	public ASTMethod(Token start, Token end, String name, String returnType, List<ASTVariable> parameters, List<String> exceptionsThrowed) {
		super(start,end);
		this.name = name;
		this.returnType = returnType;
		this.parameters = parameters;
		this.exceptionsThrowed = exceptionsThrowed;
	}

	public ASTMethod(int start, int end, String name, String returnType, List<ASTVariable> parameters, List<String> exceptionsThrowed) {
		super(start,end);
		this.name = name;
		this.returnType = returnType;
		this.parameters = parameters;
		this.exceptionsThrowed = exceptionsThrowed;
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
		if(exceptionsThrowed.size() > 0){
			out += " throws ";
			for(String v: exceptionsThrowed){
				out += v.toString() + ",";
			}
			out = out.substring(0,out.length()-1);
		}
		out += "\n";
		for(IASTStm e : stms){
			out += e.toString() + "\n";
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
}
