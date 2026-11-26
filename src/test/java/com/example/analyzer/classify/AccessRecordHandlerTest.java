package com.example.analyzer.classify;

import com.example.analyzer.model.AccessRecord;
import com.example.analyzer.model.AccessResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccessRecordHandlerTest {

    private final RecordHandler handler = new AccessRecordHandler();

    private static Map<String, String> valid() {
        Map<String, String> m = new HashMap<>();
        m.put("user", "user-42");
        m.put("door", "door-A");
        m.put("result", "DENIED");
        m.put("duration_ms", "30");
        return m;
    }

    @Test
    void acceptsCompleteRecord() {
        assertEquals(Optional.of(new AccessRecord("user-42", "door-A", AccessResult.DENIED, 30)),
                handler.handle(valid()));
    }

    @Test
    void acceptsGranted() {
        Map<String, String> m = valid();
        m.put("result", "GRANTED");
        assertEquals(AccessResult.GRANTED,
                ((AccessRecord) handler.handle(m).orElseThrow()).result());
    }

    @Test
    void ignoresExtraKeys() {
        Map<String, String> m = valid();
        m.put("ts", "x");
        m.put("badge", "b-9");
        assertTrue(handler.handle(m).isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "door", "result", "duration_ms"})
    void rejectsWhenAnyRequiredKeyIsMissing(String key) {
        Map<String, String> m = valid();
        m.remove(key);
        assertEquals(Optional.empty(), handler.handle(m));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALLOWED", "denied", "", "OK"})
    void rejectsUnknownResult(String result) {
        Map<String, String> m = valid();
        m.put("result", result);
        assertEquals(Optional.empty(), handler.handle(m));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12.5", "abc", "", "-5", "1e3", "99999999999999999999"})
    void rejectsInvalidDuration(String duration) {
        Map<String, String> m = valid();
        m.put("duration_ms", duration);
        assertEquals(Optional.empty(), handler.handle(m));
    }

    @Test
    void acceptsZeroDuration() {
        Map<String, String> m = valid();
        m.put("duration_ms", "0");
        assertTrue(handler.handle(m).isPresent());
    }
}
