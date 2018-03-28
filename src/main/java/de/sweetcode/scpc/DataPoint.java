package de.sweetcode.scpc;

import javafx.scene.chart.XYChart;

import java.util.LinkedHashMap;
import java.util.Map;

public class DataPoint {

    private final long time;

    private final Map<Type, Number> data = new LinkedHashMap<>();

    public DataPoint(long time, int fps, int players, int vehicles_total, int vehicles_ai, int vehicles_player) {
        this.time = time;

        this.data.put(Types.FPS, fps);
        this.data.put(Types.PLAYERS, players);
        this.data.put(Types.VEHICLES_TOTAL, vehicles_total);
        this.data.put(Types.VEHICLES_PLAYER, vehicles_player);
        this.data.put(Types.VEHICLES_AI, vehicles_ai);
    }

    public XYChart.Data<Number, Number> getData(Type type) {
        return new XYChart.Data<>(this.time, this.data.get(type));
    }

    public long getTime() {
        return this.time;
    }

    public enum Types implements Type {

        FPS {
            @Override
            public String getName() {
                return "FPS";
            }

            @Override
            public String getSerializationKey() {
                return "game_fps";
            }
        },
        PLAYERS {
            @Override
            public String getName() {
                return "Players";
            }

            @Override
            public String getSerializationKey() {
                return "player_count";
            }
        },
        VEHICLES_TOTAL {
            @Override
            public String getName() {
                return "Vehicles (Total)";
            }

            @Override
            public String getSerializationKey() {
                return "vehicles_total";
            }
        },
        VEHICLES_AI {
            @Override
            public String getName() {
                return "Vehicles AI";
            }

            @Override
            public String getSerializationKey() {
                return "vehicles_ai";
            }
        },
        VEHICLES_PLAYER {
            @Override
            public String getName() {
                return "Vehicles (Player)";
            }

            @Override
            public String getSerializationKey() {
                return "vehicles_player";
            }
        };

    }

    public interface Type {

        String getName();

        String getSerializationKey();

    }

}
