package intermediateModelHelper.envirorment.temporal;

import intermediateModelHelper.envirorment.temporal.structure.ExplicitResourceTimeout;
import intermediateModelHelper.envirorment.temporal.structure.TimeTimeout;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by giovanni on 07/03/2017.
 */
public class ParseExplicitResourceTimeout extends ParseCSV {

    List<ExplicitResourceTimeout> methods = new ArrayList<>();

    public ParseExplicitResourceTimeout(String path) {
        super(path);
        super.start();
    }

    public ParseExplicitResourceTimeout(File file) {
        super(file);
        super.start();
    }

    public ParseExplicitResourceTimeout(InputStream stream) {
        super(stream);
        super.start();
    }


    public List<ExplicitResourceTimeout> getMethods(){
        return methods;
    }

    @Override
    protected void handleHeader(String[] header) {
        //we do not need headers
    }

    @Override
    protected void handleRow(String className, String methodName, String[] signature) {
        methods.add(new ExplicitResourceTimeout(className, methodName, Arrays.asList(signature)));
    }

}
