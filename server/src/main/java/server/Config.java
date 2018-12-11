package server;

import java.util.Random;

public class Config {
    private static boolean debug = false;
    private static boolean lazy_loading = true;

    public static void setDebug(boolean debug) {
        Config.debug = debug;
    }

    public static boolean isDebug() {
        return debug;
    }

    static String faces[] = {
            "(╯°□°）╯︵ ┻━┻",
            "¯\\_(ツ)_/¯",
            "( ͡° ͜ʖ ͡°)",
            "ಠ_ಠ",
            "༼ つ ◕_◕ ༽つ",
            "(ง'̀-'́)ง",
            "(ಥ﹏ಥ)",
            "(ノಠ益ಠ)ノ彡┻━┻",
            "♪~ ᕕ(ᐛ)ᕗ",
            "༼ つ ಥ_ಥ ༽つ",
            "~(˘▾˘~)",
            "\\ (•◡•) /",
            "(~˘▾˘)~",
            "ヾ(⌐■_■)ノ♪",
            "\\ (•◡•) /",
            "ಠ~ಠ"
    };

    public static boolean isLazy() {
        return lazy_loading;
    }

    public static void setLazy_loading(boolean lazy_loading) {
        Config.lazy_loading = lazy_loading;
    }

    public static String[] getFaces() {
        return faces;
    }

    public static void setFaces(String[] faces) {
        Config.faces = faces;
    }

    public static String getRandomFace(){
        int i = new Random().nextInt(faces.length);
        return faces[i];
    }
}
