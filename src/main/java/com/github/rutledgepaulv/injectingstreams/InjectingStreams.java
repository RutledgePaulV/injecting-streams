package com.github.rutledgepaulv.injectingstreams;

import java.io.InputStream;
import java.io.OutputStream;

public final class InjectingStreams {
    private InjectingStreams() {
    }

    public static OutputStream injectBeforeOutput(OutputStream out, String delimiter, String injection) {
        return new PreDelimiterInjectingOutputStream(out, delimiter, injection);
    }

    public static OutputStream injectBeforeOutput(OutputStream out, String delimiter, byte[] injection) {
        return new PreDelimiterInjectingOutputStream(out, delimiter, injection);
    }

    public static OutputStream injectBeforeOutput(OutputStream out, String delimiter, InputStream injection) {
        return new PreDelimiterInjectingOutputStream(out, delimiter, injection);
    }

    public static OutputStream injectBeforeOutput(OutputStream out, byte[] delimiter, String injection) {
        return new PreDelimiterInjectingOutputStream(out, delimiter, injection);
    }

    public static OutputStream injectBeforeOutput(OutputStream out, byte[] delimiter, byte[] injection) {
        return new PreDelimiterInjectingOutputStream(out, delimiter, injection);
    }

    public static OutputStream injectBeforeOutput(OutputStream out, byte[] delimiter, InputStream injection) {
        return new PreDelimiterInjectingOutputStream(out, delimiter, injection);
    }

    public static OutputStream injectAfterOutput(OutputStream out, String delimiter, String injection) {
        return new PostDelimiterInjectingOutputStream(out, delimiter, injection);
    }

    public static OutputStream injectAfterOutput(OutputStream out, String delimiter, byte[] injection) {
        return new PostDelimiterInjectingOutputStream(out, delimiter, injection);
    }

    public static OutputStream injectAfterOutput(OutputStream out, String delimiter, InputStream injection) {
        return new PostDelimiterInjectingOutputStream(out, delimiter, injection);
    }

    public static OutputStream injectAfterOutput(OutputStream out, byte[] delimiter, String injection) {
        return new PostDelimiterInjectingOutputStream(out, delimiter, injection);
    }

    public static OutputStream injectAfterOutput(OutputStream out, byte[] delimiter, byte[] injection) {
        return new PostDelimiterInjectingOutputStream(out, delimiter, injection);
    }

    public static OutputStream injectAfterOutput(OutputStream out, byte[] delimiter, InputStream injection) {
        return new PostDelimiterInjectingOutputStream(out, delimiter, injection);
    }

}
