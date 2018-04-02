package de.sweetcode.scpc.crash;

import java.util.regex.Pattern;

public interface CrashDataType {

    String getDescription();

    Pattern getPattern();

    String getSerializationKey();

}
