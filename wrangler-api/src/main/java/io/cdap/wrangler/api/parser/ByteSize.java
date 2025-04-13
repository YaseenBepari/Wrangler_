package io.cdap.wrangler.api.parser;

public class ByteSize extends Token {
    private final long bytes;

    public ByteSize(String value) {
        super(Type.BYTE_SIZE, value);
        this.bytes = parseByteSize(value);
    }

    private long parseByteSize(String input) {
        input = input.trim().toUpperCase();
        if (input.endsWith("KB")) return Long.parseLong(input.replace("KB", "")) * 1024;
        if (input.endsWith("MB")) return Long.parseLong(input.replace("MB", "")) * 1024 * 1024;
        if (input.endsWith("GB")) return Long.parseLong(input.replace("GB", "")) * 1024 * 1024 * 1024;
        return Long.parseLong(input); // Assume bytes
    }

    public long getBytes() {
        return bytes;
    }
}
