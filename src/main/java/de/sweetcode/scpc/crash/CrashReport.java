package de.sweetcode.scpc.crash;

import java.util.LinkedHashMap;
import java.util.Map;

public class CrashReport {

    private boolean gracefullyShutdown = true;
    private final Map<CrashDataType, Boolean> crashData = new LinkedHashMap<>();

    public CrashReport() { }

    public boolean isGracefullyShutdown() {
        return this.gracefullyShutdown;
    }

    public Map<CrashDataType, Boolean> getCrashData() {
        return this.crashData;
    }

    public void add(CrashDataType type, boolean value) {
        this.crashData.put(type, value);
    }

    public void setGracefullyShutdown(boolean gracefullyShutdown) {
        this.gracefullyShutdown = gracefullyShutdown;
    }
}
