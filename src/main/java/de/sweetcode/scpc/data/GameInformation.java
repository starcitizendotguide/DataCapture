package de.sweetcode.scpc.data;

public class GameInformation {

    private final String version;
    private final String branch;

    public GameInformation(String version, String branch) {
        this.version = version;
        this.branch = branch;
    }

    public String getVersion() {
        return this.version;
    }

    public String getBranch() {
        return this.branch;
    }

    public boolean isEmpty() {
        return this.version.isEmpty() || this.branch.isEmpty();
    }

}
