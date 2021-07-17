package com.github.rutledgepaulv.injectingstreams;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.github.rutledgepaulv.injectingstreams.InjectingStreams.injectAfterOutput;
import static java.nio.charset.Charset.defaultCharset;
import static org.junit.Assert.assertEquals;

public class PostDelimiterInjectingOutputStreamTest {

    @Test
    public void constructors() {
        injectAfterOutput(new ByteArrayOutputStream(), "Test", "Test");
        injectAfterOutput(new ByteArrayOutputStream(), "Test", "Test".getBytes());
        injectAfterOutput(new ByteArrayOutputStream(), "Test".getBytes(), "Test");
        injectAfterOutput(new ByteArrayOutputStream(), "Test".getBytes(), "Test".getBytes());
        injectAfterOutput(new ByteArrayOutputStream(), "Test", new ByteArrayInputStream("Test".getBytes()));
        injectAfterOutput(new ByteArrayOutputStream(), "Test".getBytes(), new ByteArrayInputStream("Test".getBytes()));

        new PostDelimiterInjectingOutputStream(new ByteArrayOutputStream(), "Test", "Test".getBytes());
        new PostDelimiterInjectingOutputStream(new ByteArrayOutputStream(), "Test".getBytes(), "Test");
        new PostDelimiterInjectingOutputStream(new ByteArrayOutputStream(), "Test".getBytes(), "Test".getBytes());
        new PostDelimiterInjectingOutputStream(new ByteArrayOutputStream(), "Test", new ByteArrayInputStream("Test".getBytes()));
        new PostDelimiterInjectingOutputStream(new ByteArrayOutputStream(), "Test".getBytes(), new ByteArrayInputStream("Test".getBytes()));
    }

    @Test
    public void fuzzingSingleCharacterDelimiter() throws IOException {

        for (int i = 0; i < 10000; i++) {
            ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
            fuzzyWrite(new PostDelimiterInjectingOutputStream(rawOut, "h", "world "), "before hello after");
            String finalOutput = new String(rawOut.toByteArray(), defaultCharset());
            assertEquals("before hworld ello after", finalOutput);
        }

    }

    @Test
    public void fuzzingLongString() throws IOException {

        for (int i = 0; i < 10000; i++) {
            ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
            fuzzyWrite(new PostDelimiterInjectingOutputStream(rawOut, "hello ", "world "), "before hello after");
            String finalOutput = new String(rawOut.toByteArray(), defaultCharset());
            assertEquals("before hello world after", finalOutput);
        }

    }

    public void fuzzyWrite(OutputStream stream, String content) throws IOException {
        List<byte[]> parts = partitions(content);
        String combined = parts.stream().map(String::new).reduce((s1, s2) -> s1 + s2).get();
        assertEquals(content, combined);
        try (OutputStream out = stream) {
            for (byte[] bites : parts) {
                if (bites.length == 1) {
                    out.write(bites[0]);
                } else {
                    out.write(bites);
                }
            }
        }
    }

    public List<byte[]> partitions(String content) {
        Random random = new Random(System.nanoTime());
        int offset = 0;
        List<String> partitions = new ArrayList<>();
        while (offset < content.length()) {
            int length = Math.min(random.nextInt(content.length() - offset) + 1, content.length());
            partitions.add(content.substring(offset, offset + length));
            offset += length;
        }
        return partitions.stream().map(String::getBytes).collect(Collectors.toList());
    }
}