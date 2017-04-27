package instrumentation;

import javassist.*;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

;

/**
 * Created by giovanni on 27/04/2017.
 */
public class TestInstrumentation implements ClassFileTransformer  {

    String filePath = System.getProperty("user.dir") +  File.separator + "traces.txt";

    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {


        byte[] byteCode = classfileBuffer;
        if (className.equals("instrumentation/Testing")) {
            System.err.println("AGENT INJECTION : " + className);
            try {
                ClassPool cp = ClassPool.getDefault();
                CtClass cc = cp.get("instrumentation.Testing");
                injectClass(cc);
                CtMethod m = cc.getDeclaredMethod("randomSleep");
                injectMethod(m, "instrumentation.Testing","randomSleep", 14, "randomSleepDuration" );
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return byteCode;
    }

    private void injectClass(CtClass cc) throws NotFoundException {
        ClassPool cp = cc.getClassPool();
        cp.importPackage("java.io.FileWriter");
        cp.importPackage("java.io.IOException");
    }

    private void injectMethod(CtMethod m, String className, String methodName, int line, String var) throws CannotCompileException {
        m.addLocalVariable("__thID", CtClass.longType);
        m.insertBefore("__thID = Thread.currentThread().getId();");
        StringBuffer src = new StringBuffer();
        src.append("try {");
        src.append("String filename= \"" + filePath + "\";");
        src.append("FileWriter fw = new FileWriter(filename,true);");
        src.append("fw.write(__thID");
        src.append("+ \"," + className);
        src.append("," + methodName);
        src.append("," + line);
        src.append("," + var);
        src.append(",\" + " + var);
        src.append("+\"\\n\");");
        src.append("fw.close();");
        src.append("}catch (IOException e) {}");
        m.insertAt(line+1, src.toString());
    }
}
