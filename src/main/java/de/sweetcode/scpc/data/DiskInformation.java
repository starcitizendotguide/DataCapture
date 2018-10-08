package de.sweetcode.scpc.data;

import de.sweetcode.scpc.Main;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiskInformation {

    private Map<DataPoint.Type, String> data = new LinkedHashMap<>();
    private OSProcess process = null;

    public DiskInformation() {
        for(DataPoint.Type type : Types.values()) {
            this.data.put(type, "N/A");
        }
    }

    public OSProcess getProcess() {
        return this.process;
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


        this.updateProcess();
        if (this.process != null) {

            String[] pathSplit = process.getPath().split(":\\\\");

            if(pathSplit.length > 0) {
                final String mount = pathSplit[0];
                HWDiskStore disk = partitions.get(mount);

                if(disk != null) {
                    String value = disk.getModel();
                    if(value != null && !value.isEmpty()) {
                        this.add(Types.DISK_NAME, value);
                    }
                }
            }
        }

    }

    public void updateProcess() {
        OSProcess[] processes = Main.getSystemInfo().getOperatingSystem().getProcesses(Integer.MAX_VALUE, OperatingSystem.ProcessSort.CPU);
        for(OSProcess process : processes) {
            if(process.getName().equalsIgnoreCase("StarCitizen") || process.getName().equalsIgnoreCase("starcitizen.exe")) {
                this.process = process;
                break;
            }
        }
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

