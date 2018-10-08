package de.sweetcode.scpc.data;

import de.sweetcode.scpc.Main;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiskInformation {

    private final CaptureSession captureSession;
    private Map<DataPoint.Type, String> data = new LinkedHashMap<>();

    private boolean hasExtracted = false;


    public DiskInformation(CaptureSession captureSession) {
        this.captureSession = captureSession;
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

        if (hasExtracted) return;

        HWDiskStore[] diskStores = Main.getSystemInfo().getHardware().getDiskStores();
        Map<String, HWDiskStore> partitions = new HashMap<>();
        for(HWDiskStore disk : diskStores) {
            for(HWPartition partition : disk.getPartitions()) {
                String[] mountSplit = partition.getMountPoint().split(":\\\\");
                if(mountSplit.length > 0) {
                    partitions.put(mountSplit[0], disk);
                }
            }
        }

        if (this.captureSession.getProcess() != null) {

            String[] pathSplit = this.captureSession.getProcess().getPath().split(":\\\\");

            if(pathSplit.length > 0) {
                final String mount = pathSplit[0];
                HWDiskStore disk = partitions.get(mount);

                if(disk != null) {
                    String value = disk.getModel();
                    if(value != null && !value.isEmpty()) {
                        hasExtracted = true;
                        this.add(Types.DISK_NAME, value);
                    }
                }
            }
        }

    }

    public boolean hasExtracted() {
        return this.hasExtracted;
    }

    public enum Types implements DataPoint.Type {

        DISK_NAME {
            @Override
            public String getName() {
                return "Disk Name";
            }

            @Override
            public String getSerializationKey() {
                return "disk_name";
            }

            @Override
            public String getPacketKey() {
                throw new IllegalStateException();
            }

        };
    }

}

