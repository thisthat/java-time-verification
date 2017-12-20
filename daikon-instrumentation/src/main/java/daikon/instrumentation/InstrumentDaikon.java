package daikon.instrumentation;

import daikon.instrumentation.preprocess.WatchPoint;
import daikon.instrumentation.preprocess.WatchPoints;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;


/**
 * Created by giovanni on 27/04/2017.
 */
public class InstrumentDaikon implements ClassFileTransformer  {
    private static final Logger LOGGER = LogManager.getLogger();
    private static WatchPoints watchPoints = WatchPoints.getInstance();


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        //LOGGER.debug("Analysing class {}", className);
        if(watchPoints.shouldWatch(className)){
            LOGGER.debug("Editing bytecode of class {}", className);
            try {
                Set<String> getMethodDefs = watchPoints.getMethodDefinitions(className);
                CtClass cc = ClassPool.getDefault().get(className);
                //generating the invariant_%line%(_%vars%)+ methods
                DataOutputStream out = new DataOutputStream(new FileOutputStream("Base.class"));
                int version = 0;
                cc.getClassFile().write(out);
                for(String m : getMethodDefs) {
                    CtMethod mNew = CtNewMethod.make(m, cc);
                    cc.addMethod(mNew);
                    LOGGER.debug("Injected method: " + m);
                }
                //generating the method calls
                Set<WatchPoint> points = watchPoints.getWatchPoints(className);
                for(WatchPoint p : points){
                    String methodName = p.getMethodName();
                    CtMethod method = cc.getDeclaredMethod(methodName);
                    method.insertAt(p.getLine(), p.printAsMethodCall());
                    out = new DataOutputStream(new FileOutputStream("Base_" + version++ + ".class"));
                    cc.getClassFile().write(out);
                    LOGGER.debug("[V:{}]Injected call: " + p.printAsMethodCall(), version);
                }
                cc.writeFile();
                byteCode = cc.toBytecode();
                cc.detach();
                LOGGER.debug("Injection for class {} completed!", className);

            } catch (Exception e){
                LOGGER.debug("[FAIL] Injection failed");
                LOGGER.debug(e);
            }
        }
        return byteCode;
    }
}
