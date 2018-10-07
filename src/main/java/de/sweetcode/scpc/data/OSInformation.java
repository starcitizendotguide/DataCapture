package de.sweetcode.scpc.data;

import de.sweetcode.scpc.Main;
import oshi.software.os.OperatingSystem;

import java.util.LinkedHashMap;
import java.util.Map;

public class OSInformation {

    private Map<DataPoint.Type, String> data = new LinkedHashMap<>();

    public OSInformation() {
        for(DataPoint.Type type : Types.values()) {
            this.data.put(type, "N/A");
        }
    }

    public Map<DataPoint.Type, String> getData() {
        return this.data;
    }

    public String getValue(DataPoint.Type type) {
        return this.data.get(type);
    }

    public void add(DataPoint.Type type, String value) {
        this.data.put(type, value);
    }

    public void extractData() {
        OperatingSystem os = Main.getSystemInfo().getOperatingSystem();

        {
            String value = os.getManufacturer();
            if(value != null && !value.isEmpty()) {
                this.add(Types.MANUFACTURER, value);
            }
        }

        {
            String value = os.getVersion().getVersion();
            if(value != null && !value.isEmpty()) {
                this.add(Types.VERSION, value);
            }
        }

        {
            String value = os.getVersion().getBuildNumber();
            if(value != null && !value.isEmpty()) {
                this.add(Types.BUILD_NUMBER, value);
            }
        }

    }

    public enum Types implements DataPoint.Type {

        MANUFACTURER {
            @Override
            public String getName() {
                return "Manufacturer";
            }

            @Override
            public String getSerializationKey() {
                return "os_manufacturer";
            }

            @Override
            public String getPacketKey() {
                throw new IllegalStateException();
            }

        },
        VERSION {
            @Override
            public String getName() {
                return "Version";
            }

            @Override
            public String getSerializationKey() {
                return "os_version";
            }

            @Override
            public String getPacketKey() {
                throw new IllegalStateException();
            }

        },
        BUILD_NUMBER {
            @Override
            public String getName() {
                return "Build Number";
            }

            @Override
            public String getSerializationKey() {
                return "os_build_number";
            }

            @Override
            public String getPacketKey() {
                throw new IllegalStateException();
            }

        };
    }

}

