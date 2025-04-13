package io.cdap.directives;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse byte sizes and time durations into base units.
 */
public class UnitParser {
    private static final Pattern SIZE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(B|KB|MB|GB|TB)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(ns|us|ms|s|m|h)?", Pattern.CASE_INSENSITIVE);

    private static final long[] SIZE_MULTIPLIERS = {1L, 1024L, 1024L * 1024, 1024L * 1024 * 1024, 1024L * 1024 * 1024 * 1024};
    private static final long[] TIME_MULTIPLIERS = {1L, 1_000L, 1_000_000L, 1_000_000_000L, 60L * 1_000_000_000L, 3600L * 1_000_000_000L};

    private static final String[] SIZE_UNITS = {"B", "KB", "MB", "GB", "TB"};
    private static final String[] TIME_UNITS = {"ns", "us", "ms", "s", "m", "h"};

    public static long parseByteSize(String sizeStr) {
        Matcher matcher = SIZE_PATTERN.matcher(sizeStr.trim().toUpperCase(Locale.ROOT));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid byte size format: " + sizeStr);
        }

        double value = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2) == null ? "B" : matcher.group(2);

        for (int i = 0; i < SIZE_UNITS.length; i++) {
            if (unit.equals(SIZE_UNITS[i])) {
                return (long) (value * SIZE_MULTIPLIERS[i]);
            }
        }
        throw new IllegalArgumentException("Unknown size unit: " + unit);
    }

    public static long parseTimeDuration(String durationStr) {
        Matcher matcher = TIME_PATTERN.matcher(durationStr.trim().toLowerCase(Locale.ROOT));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time duration format: " + durationStr);
        }

        double value = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2) == null ? "ns" : matcher.group(2);

        for (int i = 0; i < TIME_UNITS.length; i++) {
            if (unit.equals(TIME_UNITS[i])) {
                return (long) (value * TIME_MULTIPLIERS[i]);
            }
        }
        throw new IllegalArgumentException("Unknown time unit: " + unit);
    }

    public static double convertBytes(long bytes, String toUnit) {
        toUnit = toUnit.toUpperCase(Locale.ROOT);
        for (int i = 0; i < SIZE_UNITS.length; i++) {
            if (toUnit.equals(SIZE_UNITS[i])) {
                return bytes / (double) SIZE_MULTIPLIERS[i];
            }
        }
        throw new IllegalArgumentException("Invalid byte unit for conversion: " + toUnit);
    }

    public static double convertTime(long nanos, String toUnit) {
        toUnit = toUnit.toLowerCase(Locale.ROOT);
        for (int i = 0; i < TIME_UNITS.length; i++) {
            if (toUnit.equals(TIME_UNITS[i])) {
                return nanos / (double) TIME_MULTIPLIERS[i];
            }
        }
        throw new IllegalArgumentException("Invalid time unit for conversion: " + toUnit);
    }
}
