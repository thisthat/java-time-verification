package instrumentation;

import javassist.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger LOGGER = LogManager.getLogger();

    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {


        //the idea is, once we load the correct class, we change on the fly its bytecode
        byte[] byteCode = classfileBuffer;
        if (className.equals("instrumentation/Testing")) {
            System.err.println("AGENT INJECTION : " + className);
            try {
                //retrive the class and method that we are looking for
                ClassPool cp = ClassPool.getDefault();
                CtClass cc = cp.get("instrumentation.Testing");
                //add the imports that we need for logging
                injectClass(cc);
                //instrument the code of the method
                CtMethod m = cc.getDeclaredMethod("randomSleep");
                injectMethod(m, "instrumentation.Testing", 14, "randomSleepDuration" );
                injectMethod(m, "instrumentation.Testing", 18, "abc" );
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception ex) {
                LOGGER.error("Cannot inject: {}", ex.getMessage());
            }
        }
        return byteCode;
    }

    /**
     * It adds the imports needed to write a file.
     * @param cc    Class in which insert the imports
     * @throws NotFoundException
     */
    private void injectClass(CtClass cc) throws NotFoundException {
        ClassPool cp = cc.getClassPool();
        cp.importPackage("java.io.FileWriter");
        cp.importPackage("java.io.IOException");
    }

    /**
     * It instruments the byte code of a method setting up the logging of a specific value
     * @param m             Method to instrument
     * @param className     Name of the class which holds the method
     * @param line          Line where to log the data
     * @param var           Variable name to log
     * @throws CannotCompileException
     */
    private void injectMethod(CtMethod m, String className, int line, String var) throws CannotCompileException {
        String methodName = m.getName();
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
