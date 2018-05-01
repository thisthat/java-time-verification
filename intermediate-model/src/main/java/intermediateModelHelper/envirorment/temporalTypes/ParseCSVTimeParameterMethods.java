package intermediateModelHelper.envirorment.temporalTypes;

import intermediateModelHelper.envirorment.temporal.ParseCSV;
import intermediateModelHelper.envirorment.temporalTypes.structure.TimeMethod;
import intermediateModelHelper.envirorment.temporalTypes.structure.TimeParameterMethod;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by giovanni on 07/03/2017.
 */
public class ParseCSVTimeParameterMethods extends ParseCSV {

    List<TimeParameterMethod> methods = new ArrayList<>();

    public ParseCSVTimeParameterMethods(String path) {
        super(path);
        super.setSplitSignature(true);
        super.start();
    }

    public ParseCSVTimeParameterMethods(File file) {
        super(file);
        super.setSplitSignature(true);
        super.start();
    }

    public ParseCSVTimeParameterMethods(InputStream stream) {
        super(stream);
        super.setSplitSignature(true);
        super.start();
    }

    public List<TimeParameterMethod> getMethods(){
        return methods;
    }

    @Override
    protected void start(InputStreamReader file){
        boolean notHeader = false;
        try (BufferedReader br = new BufferedReader(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] row = line.split(super.getSeparator());
                if(notHeader) {
                    if(row.length == 4){
                        int[] timeouts = Arrays.stream(row[3].split(","))
                                .map(String::trim).mapToInt(Integer::parseInt).toArray();
                        handleRow(row[0],row[1], row[2].split(","), timeouts);
                    }
                }
                else {
                    notHeader = true;
                    handleHeader(row);
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    @Override
    protected void handleHeader(String[] header) {
        //we do not need headers
    }

    @Override
    protected void handleRow(String className, String methodName, String[] signature) {
        System.err.println("We should never reach this method call");
        methods.add(new TimeParameterMethod(className, methodName, Arrays.asList(signature), new int[0]));
    }

    protected void handleRow(String className, String methodName, String[] signature, int[] timeouts) {
        methods.add(new TimeParameterMethod(className, methodName, Arrays.asList(signature), timeouts));
    }

}
