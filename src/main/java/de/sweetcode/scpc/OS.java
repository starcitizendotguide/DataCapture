package de.sweetcode.scpc;

public enum OS {

    WINDOWS,
    MAC_OS_X,
    LINUX,
    UNKNOWN;

    private final static String OS_NAME = System.getProperty("os.name");
    private static OS OS_CACHE = null;

    public static OS getPlatform() {
        return getPlatform(false);
    }
    public static OS getPlatform(boolean clearCache) {

        if(OS_NAME == null) {
            return UNKNOWN;
        }

        if(clearCache) {
            OS.clearCache();
        }

        if(!(OS_CACHE == null)) {
            return OS_CACHE;
        }

        if(OS_NAME.startsWith("Windows")) {
            return (OS_CACHE = WINDOWS);
        } else if(
                OS_NAME.startsWith("Linux")   ||
                        OS_NAME.startsWith("LINUX")   ||
                        OS_NAME.startsWith("FreeBSD") ||
                        OS_NAME.startsWith("Unix")    ||
                        OS_NAME.startsWith("SunOS")
                ) {
            return (OS_CACHE = LINUX);
        } else if(
                OS_NAME.startsWith("Mac OS X") ||
                        OS_NAME.startsWith("Darwin")
                ) {
            return (OS_CACHE = MAC_OS_X);
        }

        return (OS_CACHE = UNKNOWN);

    }

    public static void clearCache() {
        OS_CACHE = null;
    }

}
