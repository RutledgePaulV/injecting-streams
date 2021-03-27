package com.github.rutledgepaulv.injectingstreams;

import java.io.*;


/**
 * An output stream that injects a stream of bytes immediately following the first series of
 * delimiter bytes. Useful for appending content to a stream following a known delimiter.
 *
 * Assumes a single writer (no synchronization)
 */
public class InjectingOutputStream extends FilterOutputStream {

    private final InputStream injection;
    private volatile boolean injected = false;
    private volatile int bufferPos = 0;
    private final byte[] delimiter;

    public InjectingOutputStream(OutputStream out, String delimiter, String injection) {
        this(out, delimiter.getBytes(), injection.getBytes());
    }

    public InjectingOutputStream(OutputStream out, String delimiter, byte[] injection) {
        this(out, delimiter.getBytes(), injection);
    }

    public InjectingOutputStream(OutputStream out, String delimiter, InputStream injection) {
        this(out, delimiter.getBytes(), injection);
    }

    public InjectingOutputStream(OutputStream out, byte[] delimiter, String injection) {
        this(out, delimiter, injection.getBytes());
    }

    public InjectingOutputStream(OutputStream out, byte[] delimiter, byte[] injection) {
        this(out, delimiter, new ByteArrayInputStream(injection));
    }

    public InjectingOutputStream(OutputStream out, byte[] delimiter, InputStream injection) {
        super(out);
        this.delimiter = delimiter;
        this.injection = injection;
    }

    private void inject() throws IOException {
        try (InputStream in = this.injection) {
            byte[] buffer = new byte[4096];
            int n;
            while (-1 != (n = in.read(buffer))) {
                out.write(buffer, 0, n);
            }
        } finally {
            injected = true;
        }
    }

    private boolean check(byte b) {
        int pos = bufferPos;
        if (delimiter[pos] == b) {
            if (pos == (delimiter.length - 1)) {
                bufferPos = 0;
                return true;
            } else {
                bufferPos = pos + 1;
            }
        } else {
            bufferPos = 0;
        }
        return false;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        if (!injected) {
            if (check((byte) b)) {
                inject();
            }
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        this.write(bytes, 0, bytes.length);
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        if (!injected) {
            for (int i = off; i < len; i++) {
                byte bite = bytes[i];
                if (check(bite)) {
                    int preLength = i - off + 1;
                    out.write(bytes, off, preLength);
                    inject();
                    int nextIndex = i + 1;
                    if (nextIndex < len) {
                        int postLength = (len - nextIndex);
                        out.write(bytes, nextIndex, postLength);
                    }
                    return;
                }
            }
        }
        out.write(bytes, off, len);
    }

    @Override
    public void close() throws IOException {
        try {
            injection.close();
        } finally {
            super.close();
        }
    }
}
