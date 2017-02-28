package testing.classloaded;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by giovanni on 28/02/2017.
 */
public class readLibraries {

    public static void main(String[] args) throws Exception {

        URL[] urls = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs();
        for(URL u : urls){
            if(u.getFile().endsWith("jar")){
                //parseJar(u.getFile());
                System.out.println(u.getFile());
            }
        }
    }

    public static void parseJar(String file) throws Exception {
        ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                // This ZipEntry represents a class. Now, what class does it represent?
                String className = entry.getName().replace('/', '.'); // including ".class"
                String name = className.replaceAll("/", ".").substring(0, className.lastIndexOf("."));
                try {
                    Method[] methods = Class.forName(name,false, Thread.currentThread().getContextClassLoader()).getDeclaredMethods();
                    for(Method m : methods){
                        String mm = m.toGenericString();
                        String n = m.getName();
                        String r = m.getReturnType().getTypeName();
                        int pars = m.getParameterCount();
                        System.out.println(String.format("%s -- %s %s (%d)", mm, r, n , pars));
                    }
                } catch (ClassNotFoundException e){
                    //do nothing
                } catch (ExceptionInInitializerError e) {

                } catch (NoClassDefFoundError e){
                    //do nothing
                } catch (UnsatisfiedLinkError e) {
                    //do nothing
                }
            }
        }
    }
}
