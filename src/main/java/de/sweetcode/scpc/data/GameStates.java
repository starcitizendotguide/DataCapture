package de.sweetcode.scpc.data;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.util.stream.Stream;

public enum GameStates implements GameState {

    BOOTING {
        @Override
        public String getName() {
            return "Booting";
        }

        @Override
        public Image getBackground() {
            return null;
        }

        @Override
        public boolean detect(String map) {
            return (map != null && (map.equalsIgnoreCase("nomap") || map.equalsIgnoreCase("nolevel")));
        }

        @Override
        public LinearGradient getBackgroundGradient() {
            return null;
        }
    },
    MENU {
        @Override
        public String getName() {
            return "Menu";
        }

        @Override
        public Image getBackground() {
            return null;
        }

        @Override
        public boolean detect(String map) {
            return (map != null && map.equalsIgnoreCase("frontend_main"));
        }

        @Override
        public LinearGradient getBackgroundGradient() {
            return null;
        }
    },
    PU {

        private final Image image = new Image("pu.jpg");
        private final LinearGradient linearGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.1f, Color.rgb(255, 0, 0, .3)),
                new Stop(0.5f, Color.rgb(127, 0, 127, .3)),
                new Stop(1.0f, Color.rgb(0, 0, 255, .3)));

        @Override
        public String getName() {
            return "Public Universe";
        }

        @Override
        public Image getBackground() {
            return this.image;
        }

        @Override
        public boolean detect(String map) {
            return (map != null && map.equalsIgnoreCase("pu"));
        }

        @Override
        public LinearGradient getBackgroundGradient() {
            return this.linearGrad;
        }
    },
    STAR_MARINE {

        private final Image image = new Image("sm.jpg");
        private final LinearGradient linearGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.1f, Color.rgb(249, 248, 113, 0.302)),
                new Stop(0.5f, Color.rgb(255, 199, 95, 0.302)),
                new Stop(1.0f, Color.rgb(255, 150, 113, 0.302)));

        @Override
        public String getName() {
            return "Star Marine";
        }

        @Override
        public Image getBackground() {
            return this.image;
        }

        @Override
        public boolean detect(String map) {
            return (map != null && (map.equalsIgnoreCase("fps_echo11")) || map.equalsIgnoreCase("fps_demien"));
        }

        @Override
        public LinearGradient getBackgroundGradient() {
            return this.linearGrad;
        }
    },
    ARENA_COMMANDER {

        private final Image image = new Image("ac.jpg");
        private final LinearGradient linearGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.1f, Color.rgb(194, 94, 94, 0.302)),
                new Stop(0.5f, Color.rgb(96, 84, 125, 0.302)),
                new Stop(1.0f, Color.rgb(47, 72, 88, 0.302)));

        @Override
        public String getName() {
            return "Arena Commander";
        }

        @Override
        public Image getBackground() {
            return this.image;
        }

        @Override
        public boolean detect(String map) {
            return (map != null && (map.equalsIgnoreCase("dfm_brokenmoon")) || map.equalsIgnoreCase("dfm_dyingstar") || map.equalsIgnoreCase("dfm_newhorizonspeedway"));
        }

        @Override
        public LinearGradient getBackgroundGradient() {
            return this.linearGrad;
        }
    },
    UNKNOWN {
        @Override
        public String getName() {
            return "Unknown";
        }

        @Override
        public Image getBackground() {
            return null;
        }

        @Override
        public boolean detect(String map) {
            return false;
        }

        @Override
        public LinearGradient getBackgroundGradient() {
            return null;
        }
    };

    public static GameStates byName(String name) {
        return Stream.of(GameStates.values()).filter(e -> e.getName().equals(name)).findAny().orElse(GameStates.UNKNOWN);
    }

    public static GameStates byMap(String map) {
        return Stream.of(GameStates.values()).filter(e -> e.detect(map)).findAny().orElse(GameStates.UNKNOWN);
    }

}
