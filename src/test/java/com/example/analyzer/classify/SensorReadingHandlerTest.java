package com.example.analyzer.classify;

import com.example.analyzer.model.SensorReading;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SensorReadingHandlerTest {

    private final RecordHandler handler = new SensorReadingHandler();

    @Test
    void acceptsSensorWithNumericReading() {
        assertEquals(Optional.of(new SensorReading("temperature_celsius", 21.5)),
                handler.handle(Map.of("sensor", "temperature_celsius", "reading", "21.5")));
    }

    @Test
    void acceptsIntegerReading() {
        assertEquals(Optional.of(new SensorReading("humidity_percent", 45)),
                handler.handle(Map.of("sensor", "humidity_percent", "reading", "45")));
    }

    @Test
    void ignoresExtraKeys() {
        assertEquals(Optional.of(new SensorReading("t", 1.0)),
                handler.handle(Map.of("ts", "x", "sensor", "t", "reading", "1", "device", "d1")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-3.5", "+2", "0", ".5", "7.", "1e3", "2.5E-2"})
    void acceptsValidNumberFormats(String reading) {
        assertTrue(handler.handle(Map.of("sensor", "t", "reading", reading)).isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "", " ", "NaN", "Infinity", "21.5d", "0x10", "1e999", "1,5", "--1"})
    void rejectsInvalidReadings(String reading) {
        assertEquals(Optional.empty(), handler.handle(Map.of("sensor", "t", "reading", reading)));
    }

    @Test
    void rejectsMissingSensor() {
        assertEquals(Optional.empty(), handler.handle(Map.of("reading", "1")));
    }

    @Test
    void rejectsMissingReading() {
        assertEquals(Optional.empty(), handler.handle(Map.of("sensor", "t")));
    }

    @Test
    void rejectsBlankSensorName() {
        assertEquals(Optional.empty(), handler.handle(Map.of("sensor", "", "reading", "1")));
    }
}
