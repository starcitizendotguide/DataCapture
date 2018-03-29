package de.sweetcode.scpc;

import javafx.scene.chart.XYChart;

import java.util.LinkedHashMap;
import java.util.Map;

public class DataPoint {

    private final long time;

    private final Map<Type, Number> data = new LinkedHashMap<>();

    public DataPoint(long time) {
        this.time = time;
    }

    public XYChart.Data<Number, Number> getData(Type type) {
        return new XYChart.Data<>(this.time, this.data.get(type));
    }

    public long getTime() {
        return this.time;
    }

    public void add(Type type, Number number) {
        this.data.put(type, number);
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

            @Override
            public String getPacketKey() {
                return "fps";
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

            @Override
            public String getPacketKey() {
                return "count_ply";
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

            @Override
            public String getPacketKey() {
                return "veh_count_total";
            }
        },
        VEHICLES_AI {
            @Override
            public String getName() {
                return "Vehicles (AI)";
            }

            @Override
            public String getSerializationKey() {
                return "vehicles_ai";
            }

            @Override
            public String getPacketKey() {
                return "veh_count_ai";
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

            @Override
            public String getPacketKey() {
                return "veh_count_player";
            }
        };

    }

    public interface Type {

        String getName();

        String getSerializationKey();

        String getPacketKey();

    }

}
