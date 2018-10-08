package de.sweetcode.scpc.data;

import javafx.scene.chart.XYChart;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A DataPoint holds all captured values for one point in time.
 */
public class DataPoint {

    private final GameState gameState;
    private final long time;

    private final Map<Type, Number> data = new LinkedHashMap<>();

    /**
     * @param time Timestamp when the point was captured.
     */
    public DataPoint(GameState gameState, long time) {
        this.gameState = gameState;
        this.time = time;
        for(Type type : Types.values()) {
            this.data.put(type, 0);
        }
    }

    public Map<Type, Number> getData() {
        return this.data;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    /**
     * Timestamp when the data was captured.
     * @return
     */
    public long getTime() {
        return this.time;
    }

    /**
     * Creates a new XYChart.Data instance filled with the correct x and y values.
     * @param type The data point.
     * @return Never null, always a XYChart.Data instance.
     */
    public XYChart.Data<Number, Number> getData(Type type) {
        return new XYChart.Data<>(this.time, this.data.get(type));
    }


    /**
     * Adds or overrides (if the type already exists) in the data point.
     * @param type The type.
     * @param number The associated value.
     */
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
        /*PLAYERS {
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

        },*/
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
        },
        MEMORY_USAGE {
            @Override
            public String getName() {
                return "Memory Usage";
            }

            @Override
            public String getSerializationKey() {
                return "hw_memory_usage";
            }

            @Override
            public String getPacketKey() {
                return null;
            }

        },
        CPU_USAGE {
            @Override
            public String getName() {
                return "CPU Usage";
            }

            @Override
            public String getSerializationKey() {
                return "hw_cpu_usage";
            }

            @Override
            public String getPacketKey() {
                return null;
            }
        };

    }

    public interface Type {

        String getName();

        String getSerializationKey();

        String getPacketKey();

        default boolean isInPacket() {
            return (this.getPacketKey() != null);
        }

    }

}
