package de.sweetcode.scpc.data;

import java.util.stream.Stream;

public enum GameStates implements GameState {

    BOOTING {
        @Override
        public String getName() {
            return "Booting";
        }

        @Override
        public boolean detect(String map) {
            return (map != null && (map.equals("nomap") || map.equals("nolevel")));
        }
    },
    MENU {
        @Override
        public String getName() {
            return "Menu";
        }

        @Override
        public boolean detect(String map) {
            return (map != null && map.equals("frontend_main"));
        }
    },
    STAR_MARINE {
        @Override
        public String getName() {
            return "Star Marine";
        }

        @Override
        public boolean detect(String map) {
            return (map != null && (map.equals("fps_echo11")) || map.equals("fps_demien"));
        }
    },
    ARENA_COMMANDER {
        @Override
        public String getName() {
            return "Arena Commander";
        }

        @Override
        public boolean detect(String map) {
            return (map != null && (map.equals("dfm_brokenmoon")) || map.equals("dfm_dyingstar"));
        }
    },
    UNKNOWN {
        @Override
        public String getName() {
            return "Unknown";
        }

        @Override
        public boolean detect(String map) {
            return false;
        }
    };

    public static GameStates byName(String name) {
        return Stream.of(GameStates.values()).filter(e -> e.getName().equals(name)).findAny().orElse(GameStates.UNKNOWN);
    }

    public static GameStates byMap(String map) {
        return Stream.of(GameStates.values()).filter(e -> e.detect(map)).findAny().orElse(GameStates.UNKNOWN);
    }

}
