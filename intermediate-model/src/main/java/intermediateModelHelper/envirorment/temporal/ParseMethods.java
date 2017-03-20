package intermediateModelHelper.envirorment.temporal;

import intermediateModelHelper.envirorment.temporal.structure.TimeMethod;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by giovanni on 07/03/2017.
 */
public class ParseMethods extends ParseCSV {

    List<TimeMethod> methods = new ArrayList<>();

    public ParseMethods(String path) {
        super(path);
        super.start();
    }

    public ParseMethods(File file) {
        super(file);
        super.start();
    }

    public ParseMethods(InputStream stream) {
        super(stream);
        super.start();
    }


    public List<TimeMethod> getMethods(){
        return methods;
    }

    @Override
    protected void handleHeader(String[] header) {
        //we do not need headers
    }

    @Override
    protected void handleRow(String className, String methodName, String[] signature) {
        List<String> sign = Arrays.asList(signature);
        this.methods.add(new TimeMethod(className, methodName, sign));
    }

}
