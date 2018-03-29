package de.sweetcode.scpc.data;

import javafx.scene.image.Image;
import javafx.scene.paint.LinearGradient;

public interface GameState {

    String getName();

    Image getBackground();

    boolean detect(String map);

    LinearGradient getBackgroundGradient();

}
