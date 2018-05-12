package de.sweetcode.scpc.data;

import java.util.LinkedHashMap;
import java.util.Map;

public class CPUInformation {

    private Map<DataPoint.Type, String> data = new LinkedHashMap<>();

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

    public enum Types implements DataPoint.Type {

        GPU_NAME {
            @Override
            public String getName() {
                return "GPU Name";
            }

            @Override
            public String getSerializationKey() {
                return "gpu_name";
            }

            @Override
            public String getPacketKey() {
                return "gpuname";
            }

        },
        GPU_VENOR_NAME {
            @Override
            public String getName() {
                return "Vendor Name";
            }

            @Override
            public String getSerializationKey() {
                return "gpu_vendor_name";
            }

            @Override
            public String getPacketKey() {
                return "VendorName";
            }
        },
        GPU_COMPUTE_CORE_COUNT {
            @Override
            public String getName() {
                return "Compute Core Count";
            }

            @Override
            public String getSerializationKey() {
                return "gpu_compute_core_count";
            }

            @Override
            public String getPacketKey() {
                return "GPU_Compute_Core_Count";
            }
        },
        GPU_MEMORY_DEDICATED_PER_GPU {
            @Override
            public String getName() {
                return "Memory (Dedicated) per GPU";
            }

            @Override
            public String getSerializationKey() {
                return "gpu_memory_dedicated_per_gpu";
            }

            @Override
            public String getPacketKey() {
                return "gmem_mb_ded_per_gpu";
            }
        },
        GPU_MEMORY_DEDICATED {
            @Override
            public String getName() {
                return "Memory (Dedicated)";
            }

            @Override
            public String getSerializationKey() {
                return "gpu_memory_dedicated";
            }

            @Override
            public String getPacketKey() {
                return "gmem_mb_ded";
            }
        },
        GPU_TERAFLOPS {
            @Override
            public String getName() {
                return "Teraflops";
            }

            @Override
            public String getSerializationKey() {
                return "gpu_teraflops";
            }

            @Override
            public String getPacketKey() {
                return "GPU_Teraflops";
            }
        },
        GPU_CORE_CLOCK {
            @Override
            public String getName() {
                return "Core Clock";
            }

            @Override
            public String getSerializationKey() {
                return "gpu_core_clock";
            }

            @Override
            public String getPacketKey() {
                return "GPU_Core_Clock_MHz";
            }
        },
        GPU_MEMORY_CLOCK {
            @Override
            public String getName() {
                return "Memory Clock";
            }

            @Override
            public String getSerializationKey() {
                return "gpu_memory_clock";
            }

            @Override
            public String getPacketKey() {
                return "GPU_Memory_Clock_MHz";
            }
        };
    }

}
