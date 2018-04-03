package de.sweetcode.scpc.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.sweetcode.scpc.Utils;
import de.sweetcode.scpc.data.Serializer;
import de.sweetcode.scpc.gui.CaptureTab;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import okhttp3.*;

import java.io.IOException;

public class SubmitDataHandler implements EventHandler<ActionEvent> {

    private final static String BASE_HOST = "http://performance.starcitizen.guide";
    private final static Gson gson = new Gson();
    private final static OkHttpClient client = new OkHttpClient.Builder().build();

    private final MenuItem menuItem;
    private final CaptureTab captureTab;

    public SubmitDataHandler(MenuItem menuItem, CaptureTab captureTab) {
        this.menuItem = menuItem;
        this.captureTab = captureTab;
    }

    @Override
    public void handle(ActionEvent event) {

        Platform.runLater(() -> {

            final JsonObject serializedData = Serializer.serialize(this.captureTab);

            Request request = new Request.Builder()
                    .get()
                    .url(String.format("%s/api/token/generate", BASE_HOST))
                    .build();
            try(Response tokenResponse = client.newCall(request).execute()) {

                JsonObject tokenDataResponse = gson.fromJson(tokenResponse.body().string(), JsonObject.class);

                if(tokenDataResponse.get("token").isJsonNull()) {
                    Utils.popup("Data - Submit", tokenDataResponse.get("message").getAsString(), Alert.AlertType.ERROR, false);
                    return;
                }

                JsonObject submitBody = new JsonObject();
                submitBody.addProperty("token", tokenDataResponse.get("token").getAsString());
                submitBody.addProperty("value", gson.toJson(serializedData));

                Request submitRequest = new Request.Builder()
                        .post(RequestBody.create(MediaType.parse("application/json"), gson.toJson(submitBody)))
                        .url(String.format("%s/api/report/submit", BASE_HOST))
                        .build();

                Response submitResponse = client.newCall(submitRequest).execute();
                String _t = submitResponse.body().string();
                JsonObject submitResponseBody = gson.fromJson(_t, JsonObject.class);
                Utils.popup("Data - Submit", submitResponseBody.get("message").getAsString(),
                        (submitResponseBody.get("submitted").getAsBoolean() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR), false);


            } catch (IOException | JsonParseException e) {
                Utils.popup("Data - Submit", e.getMessage(), Alert.AlertType.ERROR, false);
                e.printStackTrace();
            }
        });

    }

}
