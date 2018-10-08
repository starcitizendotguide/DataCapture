package de.sweetcode.scpc.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.sweetcode.scpc.Main;
import de.sweetcode.scpc.crash.CrashReport;
import de.sweetcode.scpc.gui.CaptureTab;

import java.util.List;

public class Serializer {

    private Serializer() {}

    public static JsonObject serialize(CaptureTab captureTab) {
        JsonObject root = new JsonObject();

        //--- Data Points
        List<DataPoint> dataPoints = captureTab.getCaptureSession().getDataPoints();
        JsonArray dataPointsArray = new JsonArray();
        {
            for (DataPoint entry : dataPoints) {
                JsonObject object = new JsonObject();

                object.addProperty("index", entry.getTime());
                object.addProperty("gameState", entry.getGameState().getName());

                for (DataPoint.Type type : DataPoint.Types.values()) {
                    object.addProperty(type.getSerializationKey(), entry.getData(type).getYValue());
                }
                dataPointsArray.add(object);
            }
        }

        //--- GPU Information
        JsonObject gpuObject = new JsonObject();
        GPUInformation gpuInformation = captureTab.getCaptureSession().getGPUInformation();

        {
            gpuInformation.getData().forEach((k, v) -> gpuObject.addProperty(k.getSerializationKey(), v));
        }

        //--- CPU Information
        JsonObject cpuObject = new JsonObject();

        if(Main.FEATURE_OSHI_HARDWARE_DETECTION) {
            CPUInformation cpuInformation = captureTab.getCaptureSession().getCPUInformation();
            cpuInformation.getData().forEach((k, v) -> cpuObject.addProperty(k.getSerializationKey(), v));
        }

        //--- Disk Information
        JsonObject diskObject = new JsonObject();

        if(Main.FEATURE_OSHI_HARDWARE_DETECTION) {
            DiskInformation diskInformation = captureTab.getCaptureSession().getDiskInformation();
            diskInformation.getData().forEach((k, v) -> diskObject.addProperty(k.getSerializationKey(), v));
        }

        //--- OS Information
        JsonObject osObject = new JsonObject();

        if(Main.FEATURE_OSHI_HARDWARE_DETECTION) {
            OSInformation osInformation = captureTab.getCaptureSession().getOSInformation();
            osInformation.getData().forEach((k, v) -> osObject.addProperty(k.getSerializationKey(), v));
        }

        //--- Game Information
        JsonObject gameInformationObject = new JsonObject();
        GameInformation gameInformation = captureTab.getCaptureSession().getGameInformation();
        {
            gameInformationObject.addProperty("version", gameInformation.getVersion());
            gameInformationObject.addProperty("branch", gameInformation.getBranch());
        }

        //--- Crash Information
        JsonObject crashInformationObject = new JsonObject();
        if(Main.FEATURE_CRASH_REPORT) {
            CrashReport crashReport = captureTab.getCaptureSession().getCrashReport();

            {
                crashInformationObject.addProperty("isGracefullyShutdown", crashReport.isGracefullyShutdown());
            }

            {
                final JsonObject crashDataObject = new JsonObject();
                crashReport.getCrashData().forEach((k, v) -> crashDataObject.addProperty(k.getSerializationKey(), v));
                crashInformationObject.add("data", crashDataObject);
            }
        }


        //--- Root Building
        root.addProperty("sessionId", captureTab.getCaptureSession().getSessionId());
        root.add("game", gameInformationObject);
        root.add("gpu", gpuObject);
        root.add("cpu", cpuObject);
        root.add("disk", diskObject);
        root.add("os", osObject);
        root.add("dataPoints", dataPointsArray);

        if(Main.FEATURE_CRASH_REPORT) {
            root.add("crashReport", crashInformationObject);
        }

        return root;

    }

}
