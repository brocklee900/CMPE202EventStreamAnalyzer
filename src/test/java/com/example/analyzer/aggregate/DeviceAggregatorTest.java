package com.example.analyzer.aggregate;

import com.example.analyzer.model.DeviceEvent;
import com.example.analyzer.model.SensorReading;
import com.example.analyzer.model.Severity;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static com.example.analyzer.model.Severity.*;
import static org.junit.jupiter.api.Assertions.*;

class DeviceAggregatorTest {

    private final DeviceAggregator aggregator = new DeviceAggregator();

    private void feed(String device, Severity... severities) {
        for (Severity s : severities) {
            aggregator.accept(new DeviceEvent(device, s));
        }
    }

    @Test
    void matchesTheExampleFromTheSpec() {
        feed("thermostat-01", INFO, INFO, WARN, INFO);
        feed("door-lock-07", ERROR, CRITICAL, ERROR);

        assertEquals(Map.of(
                "by_severity", Map.of("CRITICAL", 1, "ERROR", 2, "WARN", 1, "INFO", 3),
                "by_device", Map.of(
                        "thermostat-01", Map.of("total", 4, "most_frequent_severity", "INFO"),
                        "door-lock-07", Map.of("total", 3, "most_frequent_severity", "ERROR"))),
                aggregator.result());
    }

    @Test
    void severitiesWithNoEventsAreReportedAsZero() {
        feed("d1", WARN);
        assertEquals(Map.of("CRITICAL", 0, "ERROR", 0, "WARN", 1, "INFO", 0),
                aggregator.result().get("by_severity"));
    }

    @Test
    void tieGoesToTheHigherSeverity() {
        feed("d1", INFO, WARN, INFO, WARN);
        assertEquals(WARN.name(), mostFrequentFor("d1"));
    }

    @Test
    void tieAcrossAllFourGoesToCritical() {
        feed("d1", INFO, WARN, ERROR, CRITICAL);
        assertEquals(CRITICAL.name(), mostFrequentFor("d1"));
    }

    @Test
    void higherCountBeatsHigherSeverity() {
        feed("d1", CRITICAL, INFO, INFO);
        assertEquals(INFO.name(), mostFrequentFor("d1"));
    }

    @Test
    void mostFrequentRejectsEmptyCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> DeviceAggregator.mostFrequent(new EnumMap<>(Severity.class)));
    }

    @Test
    void ignoresOtherRecordTypesAndIsEmptyByDefault() {
        aggregator.accept(new SensorReading("t", 1));
        assertEquals(Map.of(), aggregator.result());
        assertEquals("devices", aggregator.name());
    }

    @SuppressWarnings("unchecked")
    private String mostFrequentFor(String device) {
        Map<String, Object> byDevice = (Map<String, Object>) aggregator.result().get("by_device");
        return (String) ((Map<String, Object>) byDevice.get(device)).get("most_frequent_severity");
    }
}
