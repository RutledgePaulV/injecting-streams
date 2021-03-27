package com.github.rutledgepaulv.injectingstreams;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.nio.charset.Charset.defaultCharset;
import static org.junit.Assert.assertEquals;

public class InjectingOutputStreamTest {

    @Test
    public void fuzzing() throws IOException {

        for (int i = 0; i < 10000; i++) {
            ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
            fuzzyWrite(new InjectingOutputStream(rawOut, "hello ", "world "), "before hello after");
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