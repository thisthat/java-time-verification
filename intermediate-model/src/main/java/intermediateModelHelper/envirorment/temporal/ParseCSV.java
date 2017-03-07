package intermediateModelHelper.envirorment.temporal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by giovanni on 07/03/2017.
 */
public abstract class ParseCSV {

    File file;
    String separator = ";";

    public ParseCSV(String path){
        this(new File(path));
    }

    public ParseCSV(File file) {
        this.file = file;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    protected void start(){
        boolean notHeader = false;
        try (BufferedReader br = new BufferedReader(new FileReader(this.file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] row = line.split(separator);
                if(notHeader) {
                    if(row.length == 3){
                        handleRow(row[0],row[1], row[2].split(","));
                    } else {
                        handleRow(row[0],row[1], new String[0]);
                    }

                }
                else {
                    notHeader = true;
                    handleHeader(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void handleHeader(String[] header);
    protected abstract void handleRow(String className, String methodName, String[] signature);



}
