package de.sweetcode.scpc.crash;

import java.util.regex.Pattern;

public enum CrashDataTypes implements CrashDataType {


    RUN_FROM_INTERNAL {
        @Override
        public String getDescription() {
            return "run from internal";
        }

        @Override
        public Pattern getPattern() {
            return Pattern.compile("Run from internal: (No|Yes)", Pattern.CASE_INSENSITIVE) ;
        }

        @Override
        public String getSerializationKey() {
            return "run_from_internal";
        }
    },
    FATAL_ERROR {
        @Override
        public String getDescription() {
            return "A fatal error occurred.";
        }

        @Override
        public Pattern getPattern() {
            return Pattern.compile("Is fatal error: (No|Yes)", Pattern.CASE_INSENSITIVE);
        }

        @Override
        public String getSerializationKey() {
            return "fatal_error";
        }
    },
    GPU_CRASH {
        @Override
        public String getDescription() {
            return "The GPU crashed.";
        }

        @Override
        public Pattern getPattern() {
            return Pattern.compile("Is GPU crash: (No|Yes)", Pattern.CASE_INSENSITIVE);
        }

        @Override
        public String getSerializationKey() {
            return "gpu_crash";
        }
    },
    TIMEOUT {
        @Override
        public String getDescription() {
            return "Timeout";
        }

        @Override
        public Pattern getPattern() {
            return Pattern.compile("Is Timeout: (No|Yes)", Pattern.CASE_INSENSITIVE);
        }

        @Override
        public String getSerializationKey() {
            return "timeout";
        }
    },
    OUT_OF_SYSTEM_MEMORY {
        @Override
        public String getDescription() {
            return "The game run out of system memory.";
        }

        @Override
        public Pattern getPattern() {
            return Pattern.compile("Is out of system memory: (No|Yes)", Pattern.CASE_INSENSITIVE);
        }

        @Override
        public String getSerializationKey() {
            return "out_of_system_memory";
        }
    },
    OUT_OF_VIDEO_MEMORY {
        @Override
        public String getDescription() {
            return "The game run out of video memory.";
        }

        @Override
        public Pattern getPattern() {
            return Pattern.compile("Is out of video memory: (No|Yes)", Pattern.CASE_INSENSITIVE);
        }

        @Override
        public String getSerializationKey() {
            return "out_of_video_memory";
        }
    };


}
