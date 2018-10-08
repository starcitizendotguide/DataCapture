package de.sweetcode.scpc.data;

import de.sweetcode.scpc.Main;
import oshi.hardware.CentralProcessor;

import java.util.LinkedHashMap;
import java.util.Map;

public class CPUInformation {

    private Map<DataPoint.Type, String> data = new LinkedHashMap<>();

    private boolean hasExtracted = false;

    public CPUInformation() {
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

        if(hasExtracted) return;

        CentralProcessor processor = Main.getSystemInfo().getHardware().getProcessor();
        {
            String value = processor.getName();
            if(value != null && !value.isEmpty()) {
                this.add(Types.CPU_NAME, value);
                hasExtracted = true;
            }
        }
        {
            String value = processor.getVendor();
            if(value != null && !value.isEmpty()) {
                this.add(Types.CPU_VENOR_NAME, value);
            }
        }
        {
            long value = processor.getVendorFreq();
            if(value > 0) {
                this.add(Types.CPU_CORE_CLOCK, Double.toString(processor.getVendorFreq() * 1e-9));
            }
        }
        {
            int value = processor.getPhysicalProcessorCount();
            if(value > 0) {
                this.add(Types.CPU_CORE_PHYSICAL_COUNT, Integer.toString(processor.getPhysicalProcessorCount()));
            }
        }
        {
            int value = processor.getLogicalProcessorCount();
            if(value > 0) {
                this.add(CPUInformation.Types.CPU_CORE_LOGICAL_COUNT, Integer.toString(processor.getLogicalProcessorCount()));
            }
        }
    }

    public enum Types implements DataPoint.Type {

        CPU_NAME {
            @Override
            public String getName() {
                return "CPU Name";
            }

            @Override
            public String getSerializationKey() {
                return "cpu_name";
            }

            @Override
            public String getPacketKey() {
                throw new IllegalStateException();
            }

        },
        CPU_VENOR_NAME {
            @Override
            public String getName() {
                return "Vendor Name";
            }

            @Override
            public String getSerializationKey() {
                return "cpu_vendor_name";
            }

            @Override
            public String getPacketKey() {
                throw new IllegalStateException();
            }
        },
        CPU_CORE_CLOCK {
            @Override
            public String getName() {
                return "Core Clock";
            }

            @Override
            public String getSerializationKey() {
                return "cpu_core_clock";
            }

            @Override
            public String getPacketKey() {
                throw new IllegalStateException();
            }
        },
        CPU_CORE_PHYSICAL_COUNT {
            @Override
            public String getName() {
                return "Physical Core Count";
            }

            @Override
            public String getSerializationKey() {
                return "cpu_core_physical_count";
            }

            @Override
            public String getPacketKey() {
                throw new IllegalStateException();
            }
        },
        CPU_CORE_LOGICAL_COUNT {
            @Override
            public String getName() {
                return "Logical Core Count";
            }

            @Override
            public String getSerializationKey() {
                return "cpu_core_logical_count";
            }

            @Override
            public String getPacketKey() {
                throw new IllegalStateException();
            }
        };
    }

}
