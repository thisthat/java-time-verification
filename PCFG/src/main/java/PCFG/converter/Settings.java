package PCFG.converter;

/**
 * Created by giovanni on 16/02/2017.
 */
public class Settings {
    private String outputDir = System.getProperty("user.dir") + "/output/";

    private final static Settings instance = new Settings();

    protected Settings() {
    }
    public static synchronized Settings getInstance(){
        return instance;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}

