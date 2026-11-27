package com.example.analyzer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EventStreamAnalyzerTest {

    @TempDir Path tmp;

    private final ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    private final PrintStream err = new PrintStream(errBytes, true);

    @Test
    void missingFileArgumentExitsNonZeroWithMessage() {
        assertEquals(1, EventStreamAnalyzer.run(new String[]{}, err));
        assertTrue(errBytes.toString().contains("--file"));
    }

    @Test
    void nonexistentFileExitsNonZeroWithMessage() {
        assertEquals(1, EventStreamAnalyzer.run(new String[]{"--file", tmp.resolve("nope.txt").toString()}, err));
        assertTrue(errBytes.toString().contains("not found"));
    }

    @Test
    void successfulRunExitsZeroAndWritesFiles() throws Exception {
        Path input = Files.writeString(tmp.resolve("in.txt"), "ts=1 severity=INFO device=d1\n");
        Path out = tmp.resolve("out");
        assertEquals(0, EventStreamAnalyzer.run(new String[]{"--file", input.toString(), "--outdir", out.toString()}, err));
        assertTrue(Files.isRegularFile(out.resolve("devices.json")));
        assertEquals("", errBytes.toString());
    }
}
