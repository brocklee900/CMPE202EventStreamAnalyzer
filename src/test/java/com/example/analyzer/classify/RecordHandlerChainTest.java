package com.example.analyzer.classify;

import com.example.analyzer.EventStreamAnalyzer;
import com.example.analyzer.model.AccessRecord;
import com.example.analyzer.model.DeviceEvent;
import com.example.analyzer.model.EventRecord;
import com.example.analyzer.model.SensorReading;
import com.example.analyzer.model.Severity;
import com.example.analyzer.parser.KeyValueLineParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RecordHandlerChainTest {

    private final RecordHandler chain = EventStreamAnalyzer.defaultClassifier();
    private final KeyValueLineParser parser = new KeyValueLineParser();

    private Optional<EventRecord> classify(String line) {
        return parser.parse(line).flatMap(chain::handle);
    }

    @Test
    void routesEachTypeToTheRightHandler() {
        assertInstanceOf(SensorReading.class, classify("ts=1 sensor=t reading=1").orElseThrow());
        assertInstanceOf(DeviceEvent.class, classify("ts=1 severity=INFO device=d1").orElseThrow());
        assertInstanceOf(AccessRecord.class,
                classify("ts=1 user=u door=d result=GRANTED duration_ms=5").orElseThrow());
    }

    @Test
    void unmatchedLineFallsOffTheEndOfTheChain() {
        assertEquals(Optional.empty(), classify("ts=1 foo=bar"));
    }

    @Test
    void sensorLineWithDeviceKeyIsStillASensorReading() {
        assertEquals(Optional.of(new SensorReading("temperature_celsius", 21.5)),
                classify("ts=1 sensor=temperature_celsius device=thermostat-01 reading=21.5"));
    }

    @Test
    void firstMatchingHandlerWinsWhenALineFitsTwoTypes() {
        assertInstanceOf(SensorReading.class,
                classify("ts=1 sensor=t reading=1 severity=INFO device=d1").orElseThrow());
    }

    @Test
    void invalidSensorFallsThroughToALaterHandler() {
        assertEquals(Optional.of(new DeviceEvent("d1", Severity.WARN)),
                classify("ts=1 sensor=t reading=abc severity=WARN device=d1"));
    }

    @Test
    void laterHandlersAreNotAskedOnceOneMatches() {
        AtomicInteger calls = new AtomicInteger();
        RecordHandler spy = new RecordHandler() {
            @Override
            protected Optional<EventRecord> tryCreate(Map<String, String> fields) {
                calls.incrementAndGet();
                return Optional.empty();
            }
        };
        RecordHandler custom = RecordHandler.chain(new DeviceEventHandler(), spy);
        custom.handle(Map.of("severity", "INFO", "device", "d1"));
        custom.handle(Map.of("foo", "bar"));
        assertEquals(1, calls.get());
    }

    @Test
    void classifiesTheSampleInputFromTheAssignment() {
        List<String> sample = List.of(
                "ts=2024-05-10T08:00:00Z sensor=temperature_celsius device=thermostat-01 reading=21.5",
                "ts=2024-05-10T08:00:05Z severity=INFO device=thermostat-01 msg=\"Heartbeat OK\"",
                "ts=2024-05-10T08:00:10Z user=user-42 door=door-A result=DENIED duration_ms=30",
                "ts=2024-05-10T08:00:15Z sensor=humidity_percent device=hygro-02 reading=45",
                "ts=2024-05-10T08:00:20Z severity=ERROR device=door-lock-07 msg=\"Latch jammed\" code=E41",
                "ts=2024-05-10T08:00:25Z user=user-42 door=door-A result=DENIED duration_ms=28",
                "ts=2024-05-10T08:00:30Z sensor=temperature_celsius device=thermostat-02 reading=25.0",
                "ts=2024-05-10T08:00:35Z severity=WARN device=thermostat-01 msg=\"Temp drifting\"",
                "ts=2024-05-10T08:00:40Z user=user-17 door=door-B result=GRANTED duration_ms=12",
                "ts=2024-05-10T08:00:45Z user=user-42 door=door-A result=DENIED duration_ms=35",
                "ts=2024-05-10T08:00:50Z ###CORRUPTED LINE### !!!",
                "ts=2024-05-10T08:00:55Z sensor=temperature_celsius device=thermostat-01 reading=abc",
                "ts=2024-05-10T08:01:00Z severity=CRITICAL device=door-lock-07 msg=\"Tamper detected\"",
                "ts=2024-05-10T08:01:05Z user=user-08 door=door-A result=GRANTED duration_ms=18");

        List<EventRecord> records = sample.stream().map(this::classify).flatMap(Optional::stream).toList();

        assertEquals(3, records.stream().filter(SensorReading.class::isInstance).count());
        assertEquals(4, records.stream().filter(DeviceEvent.class::isInstance).count());
        assertEquals(5, records.stream().filter(AccessRecord.class::isInstance).count());
        assertEquals(12, records.size());
    }
}
