package com.example.analyzer.parser;

import org.junit.jupiter.api.Test;

import java.util.List;
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
    void parsesFullSampleLineIncludingExtraKeys() {
        Map<String, String> m = parser.parse(
                "ts=2024-05-10T08:00:20Z severity=ERROR device=door-lock-07 msg=\"Latch jammed\" code=E41")
                .orElseThrow();
        assertEquals(Map.of(
                "ts", "2024-05-10T08:00:20Z",
                "severity", "ERROR",
                "device", "door-lock-07",
                "msg", "Latch jammed",
                "code", "E41"), m);
    }

    @Test
    void preservesKeyOrder() {
        Map<String, String> m = parser.parse("ts=1 b=2 a=3").orElseThrow();
        assertEquals(List.of("ts", "b", "a"), List.copyOf(m.keySet()));
    }

    @Test
    void garbageTokensAreIgnored() {
        Map<String, String> m = parser.parse("ts=1 ###CORRUPTED LINE### !!!").orElseThrow();
        assertEquals(Map.of("ts", "1"), m);
    }

    @Test
    void lineWithNoPairsYieldsEmpty() {
        assertEquals(Optional.empty(), parser.parse("###CORRUPTED LINE### !!!"));
    }

    @Test
    void emptyLineYieldsEmpty() {
        assertEquals(Optional.empty(), parser.parse(""));
    }

    @Test
    void blankLineYieldsEmpty() {
        assertEquals(Optional.empty(), parser.parse("   \t  "));
    }

    @Test
    void nullLineYieldsEmpty() {
        assertEquals(Optional.empty(), parser.parse(null));
    }

    @Test
    void toleratesExtraWhitespaceAndTabs() {
        Map<String, String> m = parser.parse("  ts=1\t\tsensor=temp   reading=3  ").orElseThrow();
        assertEquals(Map.of("ts", "1", "sensor", "temp", "reading", "3"), m);
    }

    @Test
    void nonNumericValuesAreKeptAsText() {
        Map<String, String> m = parser.parse("ts=1 sensor=temp reading=abc").orElseThrow();
        assertEquals("abc", m.get("reading"));
    }

    @Test
    void emptyUnquotedValueIsEmptyString() {
        Map<String, String> m = parser.parse("ts=1 device=").orElseThrow();
        assertEquals("", m.get("device"));
    }

    @Test
    void emptyQuotedValueIsEmptyString() {
        Map<String, String> m = parser.parse("ts=1 msg=\"\" device=d1").orElseThrow();
        assertEquals("", m.get("msg"));
        assertEquals("d1", m.get("device"));
    }

    @Test
    void tokenWithEmptyKeyIsIgnored() {
        Map<String, String> m = parser.parse("ts=1 =orphan device=d1").orElseThrow();
        assertEquals(Map.of("ts", "1", "device", "d1"), m);
    }

    @Test
    void equalsSignInsideUnquotedValueIsKept() {
        Map<String, String> m = parser.parse("ts=1 query=a=b").orElseThrow();
        assertEquals("a=b", m.get("query"));
    }

    @Test
    void equalsSignInsideQuotedValueIsKept() {
        Map<String, String> m = parser.parse("ts=1 msg=\"x = y\"").orElseThrow();
        assertEquals("x = y", m.get("msg"));
    }

    @Test
    void unterminatedQuoteDropsTheRestOfTheLine() {
        Map<String, String> m = parser.parse("ts=1 device=d1 msg=\"never closed severity=INFO").orElseThrow();
        assertEquals(Map.of("ts", "1", "device", "d1"), m);
    }

    @Test
    void duplicateKeyKeepsLastValue() {
        Map<String, String> m = parser.parse("ts=1 device=a device=b").orElseThrow();
        assertEquals("b", m.get("device"));
    }

    @Test
    void resultIsUnmodifiable() {
        Map<String, String> m = parser.parse("ts=1").orElseThrow();
        assertThrows(UnsupportedOperationException.class, () -> m.put("x", "y"));
    }
}
