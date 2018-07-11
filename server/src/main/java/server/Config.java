package server;

public class Config {
    private static boolean debug = false;

    public static void setDebug(boolean debug) {
        Config.debug = debug;
    }

    public static boolean isDebug() {
        return debug;
    }
}
