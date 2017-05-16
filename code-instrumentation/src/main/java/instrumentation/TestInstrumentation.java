package instrumentation;

import instrumentation.data.Store;
import instrumentation.data.StoreItem;
import javassist.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

;

/**
 * Created by giovanni on 27/04/2017.
 */
public class TestInstrumentation implements ClassFileTransformer  {

    String filePath = System.getProperty("user.dir") +  File.separator + "traces.txt";
    private static final Logger LOGGER = LogManager.getLogger();
    private static Store classesToInject = Store.getInstance();

    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {


        //the idea is, once we load the correct class, we change on the fly its bytecode
        byte[] byteCode = classfileBuffer;
        if(classesToInject.containClass(className)){
            LOGGER.debug("Injected class {}", className);
            List<StoreItem> items = classesToInject.getClass(className);
            ClassPool cp = ClassPool.getDefault();
            CtClass cc = null;
            try {
                cc = cp.get(items.get(0).getClassName());
                injectClass(cc);
            } catch (Exception ex){
                LOGGER.error("Cannot inject class {}: {}", className, ex.getMessage());
            }
            for(StoreItem item : items){
                try {
                    CtMethod m = cc.getDeclaredMethod(item.getMethodName());
                    injectMethod(m, item);

                } catch (Exception ex) {
                    LOGGER.error("Cannot inject the code of method {}: {}", item, ex.getMessage());
                }
            }
            if(cc != null){
                try {
                    byteCode = cc.toBytecode();
                    cc.detach();
                }
                catch (Exception e) {
                    LOGGER.error("Cannot recompile on the fly class {}: {}", className, e.getMessage());
                }

            }

        }

        /*
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
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception ex) {
                LOGGER.error("Cannot inject: {}", ex.getMessage());
            }
        }
        */
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
     * @param m     Method to instrument
     * @param item  {@link StoreItem} which holds the info where to inject
     * @throws CannotCompileException
     */
    private void injectMethod(CtMethod m, StoreItem item) throws CannotCompileException {
        String methodName = m.getName();
        String className = item.getClassName();
        int line = item.getLine();
        String var = item.getVarName();
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