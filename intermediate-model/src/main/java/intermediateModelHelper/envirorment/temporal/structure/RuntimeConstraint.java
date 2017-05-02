package intermediateModelHelper.envirorment.temporal.structure;

/**
 * Created by giovanni on 22/03/2017.
 */
public class RuntimeConstraint {
    String className;
    String methodName;
    int line;
    String varName;

    public RuntimeConstraint(String className, String methodName, int line, String varName) {
        this.className = className;
        this.methodName = methodName;
        this.line = line;
        this.varName = varName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    @Override
    public String toString() {
        return String.format("%s;%s;%d;%s", className, methodName, line, varName);
    }
}
