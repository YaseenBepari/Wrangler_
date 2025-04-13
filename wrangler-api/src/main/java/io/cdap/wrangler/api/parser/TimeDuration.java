package io.cdap.wrangler.api.parser;

public class TimeDuration extends Token {
    private final long millis;

    public TimeDuration(String value) {
        super(Type.TIME_DURATION, value);
        this.millis = parseTime(value);
    }

    private long parseTime(String input) {
        input = input.trim().toLowerCase();
        if (input.endsWith("ms")) return Long.parseLong(input.replace("ms", ""));
        if (input.endsWith("s")) return Long.parseLong(input.replace("s", "")) * 1000;
        if (input.endsWith("m")) return Long.parseLong(input.replace("m", "")) * 60 * 1000;
        return Long.parseLong(input); // Assume ms
    }

    public long getMillis() {
        return millis;
    }
}
