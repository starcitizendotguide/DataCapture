package de.sweetcode.scpc;

import com.google.gson.Gson;
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
            Utils.popup("Exception", "An popup occured in the pcap native library. [Pcaps#getDevByAddress]", Alert.AlertType.ERROR, true);
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
            Utils.popup("Exception", "An popup occured in the pcap native library. [PcapNetworkInterface#openLive]", Alert.AlertType.ERROR, true);
            return;
        }

        while (handle.isOpen()) {

            Packet packet = null;
            try {
                packet = handle.getNextPacket();
            } catch (NotOpenException e) {
                Utils.popup("Exception", "The handle is not open.", Alert.AlertType.ERROR, false);
                return;
            } catch (IllegalArgumentException ignore) {
                ignore.printStackTrace();
                //--- No Clue @TODO
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
                        System.out.println("Captured invalid package payload: " + payload);
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
                            System.out.println(String.format("Unhandled Game State: '%s' ", finalObject.get("map").getAsString()));
                        }
                    }

                    //--- Session Handling
                    if(finalObject.has("sessionid")) {
                        long sessionId = finalObject.get("sessionid").getAsLong();

                        //--- Default Session
                        if(this.main.hasSession(-1)) {
                            captureTab = this.main.getCaptureTab(-1);
                            captureTab.getCaptureSession().setSessionId(sessionId);
                            CaptureTab finalCaptureTab1 = captureTab;
                            Platform.runLater(() -> finalCaptureTab1.setText(String.format("Session - %d", sessionId)));
                        } else if(this.main.hasSession(sessionId)) {
                            captureTab = this.main.getCaptureTab(sessionId);
                        } else if(!(this.main.hasSession(sessionId)) && !(event.equals("Game Quit"))) {
                            this.main.addCaptureSession(new CaptureSession(sessionId), false);
                            captureTab = this.main.getCaptureTab(sessionId);
                        } else {
                            //@TODO
                            throw new IllegalStateException();
                        }
                    } else {
                        System.out.println("Waiting for packet with session id...");
                        continue;
                    }

                    CaptureTab finalCaptureTab = captureTab;
                    GameState finalGameState = gameState;

                    //--- Heartbeat
                    if(event.equals("Heartbeat")) {
                        Platform.runLater(() -> {
                            DataPoint dataPoint = new DataPoint(finalGameState, finalCaptureTab.getCaptureSession().getDataPoints().size());
                            for (DataPoint.Type type : DataPoint.Types.values()) {
                                dataPoint.add(type, finalObject.get(type.getPacketKey()).getAsNumber());
                            }
                            finalCaptureTab.getCaptureSession().add(dataPoint);
                        });
                    }
                    //--- BOOT: GPU DESCRIPTION
                    else if(event.equals("boot_gpu_desc")) {
                        captureTab.setStatusText("Star Citizen is booting...", Alert.AlertType.INFORMATION);

                        GPUInformation gpuInformation = new GPUInformation();
                        for(DataPoint.Type type : GPUInformation.Types.values()) {
                            if(finalObject.has(type.getPacketKey())) {
                                gpuInformation.add(type, finalObject.get(type.getPacketKey()).getAsString());
                            } else {
                                System.out.println(String.format("boot_gpu_desc is missing '%s'", type.getPacketKey()));
                            }
                        }
                        captureTab.getCaptureSession().setGPUInformation(gpuInformation);

                    } else if(event.equals("Game Quit")) {
                        System.out.println("Game Quit -> " + payload);
                        captureTab.setStatusText("Star Citizen closed", Alert.AlertType.INFORMATION);
                    } else {
                        System.out.println("Event: " + event);
                    }

                }

            }

        }
    }
}
