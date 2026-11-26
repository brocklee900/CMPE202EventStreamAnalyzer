package com.example.analyzer.classify;

import com.example.analyzer.model.DeviceEvent;
import com.example.analyzer.model.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeviceEventHandlerTest {

    private final RecordHandler handler = new DeviceEventHandler();

    @ParameterizedTest
    @EnumSource(Severity.class)
    void acceptsEverySeverity(Severity severity) {
        assertEquals(Optional.of(new DeviceEvent("thermostat-01", severity)),
                handler.handle(Map.of("severity", severity.name(), "device", "thermostat-01")));
    }

    @Test
    void ignoresExtraKeys() {
        assertEquals(Optional.of(new DeviceEvent("door-lock-07", Severity.ERROR)),
                handler.handle(Map.of("ts", "x", "severity", "ERROR", "device", "door-lock-07",
                        "msg", "Latch jammed", "code", "E41")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEBUG", "info", "Warn", "", "WARNING"})
    void rejectsUnknownSeverity(String severity) {
        assertEquals(Optional.empty(), handler.handle(Map.of("severity", severity, "device", "d1")));
    }

    @Test
    void rejectsMissingDevice() {
        assertEquals(Optional.empty(), handler.handle(Map.of("severity", "INFO")));
    }

    @Test
    void rejectsMissingSeverity() {
        assertEquals(Optional.empty(), handler.handle(Map.of("device", "d1")));
    }

    @Test
    void rejectsBlankDevice() {
        assertEquals(Optional.empty(), handler.handle(Map.of("severity", "INFO", "device", "")));
    }
}
