package com.example.analyzer.parser;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KeyValueLineParserTest {

    private final KeyValueLineParser parser = new KeyValueLineParser();

    @Test
    void parsesSimplePairs() {
        Map<String, String> m = parser.parse("ts=1 sensor=temp reading=21.5").orElseThrow();
        assertEquals("temp", m.get("sensor"));
        assertEquals("21.5", m.get("reading"));
    }

    @Test
    void parsesQuotedValuesWithSpaces() {
        Map<String, String> m = parser.parse("ts=1 severity=INFO msg=\"Heartbeat OK\" device=d1").orElseThrow();
        assertEquals("Heartbeat OK", m.get("msg"));
        assertEquals("d1", m.get("device"));
    }

    @Test
    void garbageTokensAreIgnored() {
        Map<String, String> m = parser.parse("ts=1 ###CORRUPTED LINE### !!!").orElseThrow();
        assertEquals(1, m.size());
    }

    @Test
    void emptyLineYieldsEmpty() {
        assertEquals(Optional.empty(), parser.parse(""));
    }
}
