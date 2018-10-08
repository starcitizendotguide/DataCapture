package de.sweetcode.scpc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import de.sweetcode.scpc.data.*;
import de.sweetcode.scpc.gui.CaptureTab;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import oshi.hardware.CentralProcessor;
import oshi.software.os.OSProcess;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The CaptureDevice is the object listing to the network traffic and does all of the 'heavy-lifting'.
 */
public class CaptureDevice implements Runnable {

    private final static Pattern pattern = Pattern.compile("^(.+)(Host: launcher2\\.robertsspaceindustries\\.com).+(User-Agent: libcurl-agent\\/[0-9]\\.[0-9]).+(Accept: application\\/json).+(Content-Type: application\\/json).+(Content-Length: [0-9]+)(.+)$", Pattern.DOTALL);
    private final static Gson gson = new Gson();

    private final Main main;
    private final String addressInput;

    private final long RESERVED_SESSION_ID = 0xFFFFFFD6;

    /**
     * @param main The associated main class.
     * @param addressInput The address the user put into the the popup when the application asked him to.
     */
    public CaptureDevice(Main main, String addressInput) {
        this.main = main;
        this.addressInput = addressInput;
    }

    @Override
    public void run() {

        //---
        InetAddress address = null;
        try {
            address = InetAddress.getByName(this.addressInput);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Utils.popup("Error", "Invalid IP Address.", Alert.AlertType.ERROR, true);
        }

        PcapNetworkInterface networkInterface = null;
        try {
            networkInterface = Pcaps.getDevByAddress(address);
        } catch (PcapNativeException e) {
            e.printStackTrace();
            Utils.popup("Exception", "An popup occurred in the pcap native library. [Pcaps#getDevByAddress]", Alert.AlertType.ERROR, true);
        }

        if(networkInterface == null) {
            Utils.popup("Error", "Couldn't find a network interface.", Alert.AlertType.ERROR, true);
            return;
        }

        final int SNAP_LEN = 65536;
        final int TIMEOUT = 0;
        final PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;

        PcapHandle handle = null;
        try {
            handle = networkInterface.openLive(SNAP_LEN, mode, TIMEOUT);
        } catch (PcapNativeException e) {
            Utils.popup("Exception", "An popup occurred in the pcap native library. [PcapNetworkInterface#openLive]", Alert.AlertType.ERROR, true);
            return;
        }

        while (handle.isOpen()) {

            Packet packet = null;
            try {
                packet = handle.getNextPacket();
            } catch (NotOpenException e) {
                Utils.popup("Exception", "The handle is not open.", Alert.AlertType.ERROR, false);
                this.main.logToDebugConsole(e);
                return;
            } catch (IllegalArgumentException ignore) {
                this.main.logToDebugConsole(String.format(":UnexplainableError - %s", ignore.getMessage()));
            }

            if (packet == null) {
                continue;
            }

            if (packet.contains(IpV4Packet.class) || packet.contains(IpV6Packet.class)) {
                String raw = new String(packet.getPayload().getRawData());

                Matcher matcher = pattern.matcher(raw);

                if (matcher.matches() && matcher.groupCount() == 7) {
                    JsonObject object = null;
                    String payload = matcher.group(7).trim();
                    try {
                         object = gson.fromJson(payload, JsonObject.class);
                    } catch (JsonSyntaxException exception) {
                        this.main.logToDebugConsole("Captured invalid package payload: " + payload);
                    }

                    if(object == null) continue;

                    GameState gameState = GameStates.UNKNOWN;
                    final String event = object.get("Event").getAsString();
                    final JsonObject finalObject = object;
                    CaptureTab captureTab = null;

                    //--- Determine GameState
                    if(finalObject.has("map")) {
                        gameState = GameStates.byMap(finalObject.get("map").getAsString());

                        if(gameState == GameStates.UNKNOWN) {
                            this.main.logToDebugConsole(String.format("Unhandled Game State: '%s' ", finalObject.get("map").getAsString()));
                        }
                    } else if(finalObject.has("Map")) {
                        gameState = GameStates.byMap(finalObject.get("Map").getAsString());

                        if(gameState == GameStates.UNKNOWN) {
                            this.main.logToDebugConsole(String.format("Unhandled Game State: '%s' ", finalObject.get("map").getAsString()));
                        }
                    }

                    //--- Session Handling
                    if(finalObject.has("sessionid")) {
                        JsonElement sessionIdElement = finalObject.get("sessionid");
                        long sessionId;
                        if(sessionIdElement.getAsString().isEmpty()) {
                            sessionId = RESERVED_SESSION_ID;
                        } else {
                            sessionId = sessionIdElement.getAsLong();
                        }

                        //--- A session with the session id exists, we can just crab the tab
                        if (this.main.hasSession(sessionId)) {
                            captureTab = this.main.getCaptureTab(sessionId);
                        }
                        //--- NOTE: We received a packet with an empty sessionid field. We assigned the RESERVED_SESSION_ID
                        // to this session and update it as soon as we get the real session id!
                        else if(!(this.main.hasSession(sessionId)) && this.main.hasSession(RESERVED_SESSION_ID)) {
                            captureTab = this.main.getCaptureTab(RESERVED_SESSION_ID);
                            captureTab.getCaptureSession().setSessionId(sessionId);
                            this.main.logToDebugConsole(String.format("Updated RESERVED_SESSION_ID to %d!", sessionId));
                        }
                        //--- We don't have this id stored yet and we are also not quitting the game -> create a new session.
                        else if (!(this.main.hasSession(sessionId)) && !(event.equals("Game Quit"))) {
                            this.main.addCaptureSession(new CaptureSession(sessionId, false));
                            captureTab = this.main.getCaptureTab(sessionId);
                        } else {
                            //@TODO
                            throw new IllegalStateException();
                        }

                    } else {
                        this.main.logToDebugConsole("Waiting for packet with session id...");
                        continue;
                    }

                    CaptureTab finalCaptureTab = captureTab;
                    GameState finalGameState = gameState;

                    //--- Version
                    if(
                        captureTab.getCaptureSession().getGameInformation().isEmpty() &&
                        finalObject.has("Version") &&
                        finalObject.has("Branch")
                    ) {
                        captureTab.getCaptureSession().setGameInformation(new GameInformation(
                                finalObject.get("Version").getAsString(),
                                finalObject.get("Branch").getAsString()
                        ));
                    }

                    //--- Heartbeat
                    boolean handled = true;
                    switch (event.toLowerCase()) {

                        //--- HEARTBEAT
                        case "heartbeat":
                            Platform.runLater(() -> {
                                CaptureSession captureSession = finalCaptureTab.getCaptureSession();
                                final int id = captureSession.getDataPoints().size();
                                DataPoint dataPoint = new DataPoint(finalGameState, id);
                                for (DataPoint.Type type : DataPoint.Types.values()) {
                                    if (type.isInPacket()) {
                                        dataPoint.add(type, finalObject.get(type.getPacketKey()).getAsNumber());
                                    }
                                }

                                if(Main.FEATURE_OSHI_HARDWARE_DETECTION) {
                                    captureSession.getCPUInformation().extractData();
                                    captureSession.getDiskInformation().extractData();
                                    captureSession.updateProcess();

                                    OSProcess process = captureSession.getProcess();
                                    if(process != null) {
                                        //--- Memory Usage
                                        dataPoint.add(DataPoint.Types.MEMORY_USAGE, process.getResidentSetSize() * 1e-9);

                                        //--- CPU Usage
                                        CentralProcessor cpu = Main.getSystemInfo().getHardware().getProcessor();
                                        if(cpu != null) {
                                            int cores = cpu.getLogicalProcessorCount();

                                            if(cores != 0) {
                                                long currentCPUTime = process.getKernelTime() + process.getUserTime();
                                                long lastCheckTime = System.currentTimeMillis();

                                                if(captureSession.getDeltaCPUTime() != -1) {
                                                    long timeDelta = currentCPUTime - captureSession.getDeltaCPUTime();
                                                    dataPoint.add(DataPoint.Types.CPU_USAGE, (
                                                            (100D * ((double)timeDelta / (lastCheckTime - captureSession.getLastCPUCheckTime()))) / cores
                                                        ));
                                                    captureSession.setLastCPUCheckTime(lastCheckTime);
                                                }
                                                captureSession.setDeltaCPUTime(currentCPUTime);

                                            }
                                        }
                                    }
                                }

                                finalCaptureTab.getCaptureSession().add(dataPoint);
                            });
                            break;

                        //--- BOOT: GPU DESCRIPTION
                        case "boot_gpu_desc":
                            GPUInformation gpuInformation = captureTab.getCaptureSession().getGPUInformation();
                            for (DataPoint.Type type : GPUInformation.Types.values()) {
                                if (finalObject.has(type.getPacketKey())) {
                                    gpuInformation.add(type, finalObject.get(type.getPacketKey()).getAsString());
                                    gpuInformation.setHasExtracted(true);
                                } else {
                                    this.main.logToDebugConsole(String.format("boot_gpu_desc is missing '%s'", type.getPacketKey()));
                                }
                            }
                            break;

                        //---
                        case "game quit":
                            this.main.logToDebugConsole(("Game Quit -> " + payload));
                            captureTab.setStatusText("Capturing stopped", Alert.AlertType.INFORMATION);
                            break;

                        default: handled = false; break;
                    }

                    this.main.logToDebugConsole(String.format("%s Event: %s -> %s", (handled ? "[HANDLED]" : "[UNHANDLED]"), event, payload));

                }

            }

        }
    }
}
