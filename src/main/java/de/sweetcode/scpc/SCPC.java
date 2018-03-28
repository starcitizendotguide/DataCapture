package de.sweetcode.scpc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SCPC implements Runnable {

    private final static Pattern pattern = Pattern.compile("^(.+)(Host: launcher2\\.robertsspaceindustries\\.com).+(User-Agent: libcurl-agent\\/[0-9]\\.[0-9]).+(Accept: application\\/json).+(Content-Type: application\\/json).+(Content-Length: [0-9]+)(.+)$", Pattern.DOTALL);
    private final static Gson gson = new Gson();

    private final Main main;
    private final Capture capture;
    private final String addressInput;

    public SCPC(Main main, Capture capture, String addressInput) {
        this.main = main;
        this.capture = capture;
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
            this.main.setStatusText("An popup occured in the pcap native library. [Pcaps#getDevByAddress]", Alert.AlertType.ERROR);
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
            this.main.setStatusText("An popup occured in the pcap native library. [PcapNetworkInterface#openLive]", Alert.AlertType.ERROR);
            return;
        }

        while (handle.isOpen()) {

            Packet packet = null;
            try {
                packet = handle.getNextPacket();
            } catch (NotOpenException e) {
                this.main.setStatusText("The handle is not open.", Alert.AlertType.ERROR);
            } catch (IllegalArgumentException ignore) {
                ignore.printStackTrace();
                //--- No Clue @TODO
            }

            if (packet == null) {
                System.out.println("Packet is null.");
                continue;
            }

            if (packet.contains(IpV4Packet.class)) {
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

                    final String event = object.get("Event").getAsString();

                    if(event.equals("Heartbeat")) {
                        System.out.println("Capture");
                        final JsonObject finalObject = object;
                        Platform.runLater(() -> this.capture.add(new DataPoint(
                                this.capture.size(),
                                finalObject.get("fps").getAsInt(),
                                finalObject.get("count_ply").getAsInt(),
                                finalObject.get("veh_count_total").getAsInt(),
                                finalObject.get("veh_count_ai").getAsInt(),
                                finalObject.get("veh_count_player").getAsInt()
                        )));
                    } else if(event.equals("boot_gpu_desc")) {
                        this.main.setStatusText("Star Citizen is booting...", Alert.AlertType.INFORMATION);
                    } else if(event.equals("Game Quit")) {
                        this.main.setStatusText("Star Citizen closed", Alert.AlertType.INFORMATION);
                        handle.close();
                        this.main.start(this.addressInput);
                    } else {
                        System.out.println("Event: " + event);
                    }

                }

            }

        }
    }
}
