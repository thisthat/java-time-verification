package trash;

import java.util.HashMap;

public class Test {

    static private HashMap<String, Object> metadata = new HashMap<>();


    public static  <T extends Object> T getMeta(String name, T defaultValue) {
        try {
            return (T) metadata.get(name);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    public static void main(String[] args) {
        double x = getMeta("name", 5.0);
    }

}