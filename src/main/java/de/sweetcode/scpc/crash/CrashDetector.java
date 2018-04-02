package de.sweetcode.scpc.crash;

import de.sweetcode.scpc.OS;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrashDetector implements Runnable {

    private final static Pattern DETECT_CRASH = Pattern.compile("Cloud Imperium Games public crash handler taking over\\.\\.\\.");


    //---
    private boolean isRunning = false;

    private File exeFile = null;
    private File gameDirectory = null;

    //---
    private List<Listener> listeners = new ArrayList<>();

    public CrashDetector() {}

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void run() {

        switch (OS.getPlatform()) {

            case WINDOWS: {

                try {
                    String line;
                    Process p = Runtime.getRuntime().exec("wmic process where \"name='StarCitizen.exe'\" get ExecutablePath /FORMAT:LIST");
                    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ((line = input.readLine()) != null) {

                        if(!(line.trim().isEmpty()) && line.contains("StarCitizen.exe")) {
                            this.isRunning = true;
                            this.exeFile = new File(line.split("ExecutablePath=")[1]);
                            this.gameDirectory = new File(this.exeFile.getAbsolutePath().split("Bin64\\\\StarCitizen.exe")[0]);
                            break;
                        } else {
                            this.isRunning = false;
                        }

                    }

                    if(!this.isRunning) {
                        this.gatherSessionInformation();
                    }

                    input.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } break;

            default: throw new UnsupportedOperationException(String.format("Unsupported OS %s.", OS.getPlatform().name()));

        }

    }

    private void gatherSessionInformation() {

        if(this.exeFile == null || this.gameDirectory == null) {
            this.triggerListeners(new CrashReport());
            return;
        }

        //File file = new File(String.format("%s%s%s", this.gameDirectory.getAbsolutePath(), File.separator, "Game.log"));
        File file = new File("S:\\RSI\\StarCitizen\\LIVE\\logbackups\\Game Build(738964) 01 Apr 18 (11 29 49).log");
        CrashReport crashReport = new CrashReport();
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())), Charset.forName("UTF-8"));


            Matcher matcher = DETECT_CRASH.matcher(content);

            crashReport.setGracefullyShutdown(!matcher.find());
            {
                if (!crashReport.isGracefullyShutdown()) {
                    for (CrashDataType type : CrashDataTypes.values()) {
                        Matcher matchType = type.getPattern().matcher(content);
                        if (matchType.find()) {
                            crashReport.add(type, (matchType.group(1).equalsIgnoreCase("Yes")));
                        }
                    }
                }
            }


            this.triggerListeners(crashReport);
        } catch (IOException e) {
            this.triggerListeners(crashReport);
            e.printStackTrace();
        }


    }

    private void triggerListeners(CrashReport crashReport) {
        this.listeners.forEach(e -> e.completed(crashReport));
    }

    public interface Listener {

        void completed(CrashReport crashReport);

    }

}
