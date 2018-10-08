package de.sweetcode.scpc.generator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.sweetcode.scpc.Main;
import de.sweetcode.scpc.data.*;
import de.sweetcode.scpc.gui.BackgroundLineChart;
import de.sweetcode.scpc.gui.CaptureTab;
import okhttp3.*;
import org.apache.commons.math3.stat.StatUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;


public class RedditPostGenerator {

    private static OkHttpClient client = new OkHttpClient();
    private static Gson gson = new Gson();


    public static String generate(CaptureTab captureTab) {

        CaptureSession session = captureTab.getCaptureSession();
        StringBuilder builder = new StringBuilder();

        //--- Game Information
        {
            GameInformation gameInformation = session.getGameInformation();
            OSInformation osInformation = session.getOSInformation();
            builder.append(String.format("\nSession (%d) recorded %s data points (DP) in Star Citizen %s (%s) on a %s machine.\n\n",
                    session.getSessionId(),
                    session.getDataPoints().size(),
                    gameInformation.getVersion(),
                    gameInformation.getBranch(),
                    String.format("%s %s (%s)",
                            osInformation.getValue(OSInformation.Types.MANUFACTURER),
                            osInformation.getValue(OSInformation.Types.VERSION),
                            osInformation.getValue(OSInformation.Types.BUILD_NUMBER))
            ));
        }

        //--- GPU Information
        {
            GPUInformation gpuInformation = session.getGPUInformation();
            builder.append("____\n");
            builder.append("| GPU |  |\n");
            builder.append("|:-----------|:------------:|\n");
            for (DataPoint.Type type : GPUInformation.Types.values()) {
                builder.append(String.format("|%s|%s|\n", type.getName(), gpuInformation.getValue(type)));
            }
        }

        //--- CPU Information
        {
            CPUInformation cpuInformation = session.getCPUInformation();
            builder.append("____\n");
            builder.append("| CPU |  |\n");
            builder.append("|:-----------|:------------:|\n");
            for (DataPoint.Type type : CPUInformation.Types.values()) {
                builder.append(String.format("|%s|%s|\n", type.getName(), cpuInformation.getValue(type)));
            }
        }

        //--- Disk Information
        {
            DiskInformation diskInformation = session.getDiskInformation();
            builder.append("____\n");
            builder.append("| Disk |  |\n");
            builder.append("|:-----------|:------------:|\n");
            for (DataPoint.Type type : DiskInformation.Types.values()) {
                builder.append(String.format("|%s|%s|\n", type.getName(), diskInformation.getValue(type)));
            }
        }

        //--- Frames
        {
            builder.append("____\n");
            appendFPSTable(GameStates.PU, session, builder);
            appendFPSTable(GameStates.ARENA_COMMANDER, session, builder);
            appendFPSTable(GameStates.STAR_MARINE, session, builder);
            appendFPSTable(GameStates.HANGAR, session, builder);
            appendFPSTable(GameStates.UNKNOWN, session, builder);
        }


        //--- Screenshot
        if(!Main.GENERATOR_DISABLE_IMAGE_AND_DATA_EXPORT) {
            String graphLink = "Graph Image (Error)";
            BufferedImage screenshot = captureTab.getCaptureSessionChart().screenshot(1920, 1080, BackgroundLineChart.BackgroundType.COLOUR);
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                ImageIO.write(screenshot, "png", outputStream);
                String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());

                Request request = new Request.Builder()
                        .url("https://api.imgur.com/3/image")
                        .addHeader("Authorization", "Client-ID ced5163bd9b25b1")
                        .post(new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("image", base64)
                                .build())
                        .build();

                Response response = client.newCall(request).execute();
                JsonObject object = gson.fromJson(response.body().string(), JsonObject.class);

                if (response.code() == 200 && object.has("success") && object.get("success").getAsBoolean()) {
                    graphLink = String.format("[Graph Image](%s)", object.getAsJsonObject("data").get("link").getAsString());
                }
            } catch (final Exception ex) {
                ex.printStackTrace();
            }

            //--- Submit Data
            String jsonLink = "Full Data Set (Error)";
            try {
                Request request = new Request.Builder()
                        .url("https://api.myjson.com/bins")
                        .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), gson.toJson(Serializer.serialize(captureTab))))
                        .build();

                Response response = client.newCall(request).execute();
                if (response.code() == 201) {
                    JsonObject object = gson.fromJson(response.body().string(), JsonObject.class);
                    if (object.has("uri")) {
                        jsonLink = String.format("[Full Data Set](%s)", object.get("uri").getAsString());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            builder.append(String.format("____\n%s | %s", graphLink, jsonLink));
        }

        return builder.toString();

    }

    private static void appendFPSTable(GameState gameState, CaptureSession session, StringBuilder builder) {
        double[] fps = session.getDataPoints().stream().filter(e -> e.getGameState() == gameState).mapToDouble(
                e -> e.getData(DataPoint.Types.FPS).getYValue().doubleValue()
        ).toArray();
        double[] memory_usage = session.getDataPoints().stream().filter(e -> e.getGameState() == gameState).mapToDouble(
                e -> e.getData(DataPoint.Types.MEMORY_USAGE).getYValue().doubleValue()
        ).toArray();
        double[] cpu_usage = session.getDataPoints().stream().filter(e -> e.getGameState() == gameState).mapToDouble(
                e -> e.getData(DataPoint.Types.CPU_USAGE).getYValue().doubleValue()
        ).toArray();

        //--- Ignore empty data sets
        if(fps.length == 0 && memory_usage.length == 0 && cpu_usage.length == 0) return;

        builder.append("\n");
        builder.append(String.format("| %s (%d DP) | FPS  | Memory | CPU|\n", gameState.getName(), fps.length));
        builder.append("|:-----------|:------------:|:------------:|:------------:|\n");

        builder.append(String.format("|Mean|%s|%s|%s|\n",
                (fps.length > 0 ? String.format("%.2fFPS", StatUtils.mean(fps)) : "N/A"),
                (memory_usage.length > 0 ? String.format("%.2fGB", StatUtils.mean(memory_usage)) : "N/A"),
                (cpu_usage.length > 0 ? String.format("%.2f%%", StatUtils.mean(cpu_usage)) : "N/A")
        ));
        builder.append(String.format("|Median|%s|%s|%s|\n",
                (fps.length > 0 ? String.format("%.2fFPS", StatUtils.percentile(fps, 50)) : "N/A"),
                (memory_usage.length > 0 ? String.format("%.2fGB", StatUtils.percentile(memory_usage, 50)) : "N/A"),
                (cpu_usage.length > 0 ? String.format("%.2f%%", StatUtils.percentile(cpu_usage, 50)) : "N/A")
        ));
        builder.append(String.format("|Range|%s|%s|%s|\n",
                (fps.length > 0 ? String.format("[%.2f;%.2f]", StatUtils.min(fps), StatUtils.max(fps)) : "N/A"),
                (memory_usage.length > 0 ? String.format("[%.2f;%.2f]", StatUtils.min(memory_usage), StatUtils.max(memory_usage)) : "N/A"),
                (cpu_usage.length > 0 ? String.format("[%.2f;%.2f]", StatUtils.min(cpu_usage), StatUtils.max(cpu_usage)) : "N/A")
        ));
        builder.append(String.format("|95%%|%s|%s|%s|\n",
                (fps.length > 0 ? String.format("%.2fFPS", StatUtils.percentile(fps, 5)) : "N/A"),
                (memory_usage.length > 0 ? String.format("%.2fGB", StatUtils.percentile(memory_usage, 5)) : "N/A"),
                (cpu_usage.length > 0 ? String.format("%.2f%%", StatUtils.percentile(memory_usage, 5)) : "N/A")
        ));
        builder.append(String.format("|93%%|%s|%s|%s|\n",
                (fps.length > 0 ? String.format("%.2fFPS", StatUtils.percentile(fps, 3)) : "N/A"),
                (memory_usage.length > 0 ? String.format("%.2fGB", StatUtils.percentile(memory_usage, 3)) : "N/A"),
                (cpu_usage.length > 0 ? String.format("%.2f%%", StatUtils.percentile(memory_usage, 3)) : "N/A")
        ));
        builder.append(String.format("|99%%|%s|%s|%s|\n",
                (fps.length > 0 ? String.format("%.2fFPS", StatUtils.percentile(fps, 1)) : "N/A"),
                (memory_usage.length > 0 ? String.format("%.2fGB", StatUtils.percentile(memory_usage, 1)) : "N/A"),
                (cpu_usage.length > 0 ? String.format("%.2f%%", StatUtils.percentile(memory_usage, 1)) : "N/A")
        ));
        builder.append(String.format("|99.9%%|%s|%s|%s|\n",
                (fps.length > 0 ? String.format("%.2fFPS", StatUtils.percentile(fps, 0.1)) : "N/A"),
                (memory_usage.length > 0 ? String.format("%.2fGB", StatUtils.percentile(memory_usage, 0.1)) : "N/A"),
                (cpu_usage.length > 0 ? String.format("%.2f%%", StatUtils.percentile(memory_usage, 0.1)) : "N/A")
        ));
    }

}
