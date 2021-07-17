package com.github.rutledgepaulv.injectingstreams;

import java.io.*;


/**
 * An output stream that injects a stream of bytes immediately preceding the first series of
 * delimiter bytes.
 * <p>
 * Assumes a single writer (no synchronization)
 */
public class PreDelimiterInjectingOutputStream extends FilterOutputStream {

    private final InputStream injection;
    private boolean injected = false;
    private final byte[] delimiter;
    private int bufferOffset = 0;

    public PreDelimiterInjectingOutputStream(OutputStream out, String delimiter, String injection) {
        this(out, delimiter.getBytes(), injection.getBytes());
    }

    public PreDelimiterInjectingOutputStream(OutputStream out, String delimiter, byte[] injection) {
        this(out, delimiter.getBytes(), injection);
    }

    public PreDelimiterInjectingOutputStream(OutputStream out, String delimiter, InputStream injection) {
        this(out, delimiter.getBytes(), injection);
    }

    public PreDelimiterInjectingOutputStream(OutputStream out, byte[] delimiter, String injection) {
        this(out, delimiter, injection.getBytes());
    }

    public PreDelimiterInjectingOutputStream(OutputStream out, byte[] delimiter, byte[] injection) {
        this(out, delimiter, new ByteArrayInputStream(injection));
    }

    public PreDelimiterInjectingOutputStream(OutputStream out, byte[] delimiter, InputStream injection) {
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
            bufferOffset = 0;
        }
    }

    private void drainBuffer() throws IOException {
        if (bufferOffset > 0) {
            out.write(delimiter, 0, bufferOffset);
            bufferOffset = 0;
        }
    }

    @Override
    public void write(int b) throws IOException {
        if (!injected) {
            // if this byte is the next element of the delimiter
            if (b == delimiter[bufferOffset]) {
                // increment the buffer offset
                bufferOffset++;
                // if we've reached the end of the delimiter
                if (bufferOffset == delimiter.length) {
                    // inject the content
                    inject();
                    // inject the buffer
                    out.write(delimiter);
                }
            } else {
                // flush the pending buffer
                this.drainBuffer();
                // if this byte is the first element of another potential delimiter sequence
                if (b == delimiter[bufferOffset]) {
                    bufferOffset++;
                } else {
                    out.write(b);
                }
            }
        } else {
            out.write(b);
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        this.write(bytes, 0, bytes.length);
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        if (!injected) {
            for (int i = off; i < len + off; i++) {
                byte b = bytes[i];
                // b matches next position in buffer
                if (b == delimiter[bufferOffset]) {
                    bufferOffset++;
                    // buffer became full
                    if (bufferOffset == delimiter.length) {
                        int length = i - off - delimiter.length + 1;
                        if (length > 0) {
                            out.write(bytes, off, length);
                        }
                        inject();
                        out.write(delimiter);
                        int nextIndex = i + 1;
                        if (nextIndex < len) {
                            int remaining = len - nextIndex - off;
                            out.write(bytes, nextIndex, remaining);
                        }
                        return;
                    }
                } else {
                    // we read a byte that negates the current match, go ahead and flush
                    // the imaginary buffer if it might contain bytes from the prior write
                    if (i - bufferOffset < off) {
                        this.drainBuffer();
                    }
                    bufferOffset = 0;
                    // check if this byte is the beginning of the next sequence too
                    if (b == delimiter[bufferOffset]) {
                        bufferOffset++;
                    }
                }
            }

            // still not injected, need to take care of earlier bytes up to buffer
            if (!injected) {
                int length = (len - bufferOffset);
                if (length > 0) {
                    out.write(bytes, off, length);
                }
            }
        } else {
            out.write(bytes, off, len);
        }
    }

    @Override
    public void close() throws IOException {
        this.drainBuffer();
        try {
            injection.close();
        } finally {
            super.close();
        }
    }
}
