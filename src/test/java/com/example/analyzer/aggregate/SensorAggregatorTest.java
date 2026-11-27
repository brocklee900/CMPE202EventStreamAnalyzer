package com.example.analyzer.aggregate;

import com.example.analyzer.model.DeviceEvent;
import com.example.analyzer.model.SensorReading;
import com.example.analyzer.model.Severity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SensorAggregatorTest {

    private final SensorAggregator aggregator = new SensorAggregator();

    private void feed(String sensor, double... readings) {
        for (double r : readings) {
            aggregator.accept(new SensorReading(sensor, r));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> statsFor(String sensor) {
        return (Map<String, Object>) aggregator.result().get(sensor);
    }

    @Test
    void matchesTheHumidityExampleFromTheSpec() {
        feed("humidity_percent", 40, 50);
        assertEquals(Map.of(
                "count", 2,
                "minimum", 40L,
                "median", 45L,
                "average", 45L,
                "maximum", 50L,
                "std_dev", 5L), statsFor("humidity_percent"));
    }

    @Test
    void roundsDecimalsToOnePlace() {
        feed("temperature_celsius", 18.5, 20.0, 22.0, 25.0);
        Map<String, Object> s = statsFor("temperature_celsius");
        assertEquals(4, s.get("count"));
        assertEquals(18.5, s.get("minimum"));
        assertEquals(21L, s.get("median"));
        assertEquals(21.4, s.get("average"));
        assertEquals(25L, s.get("maximum"));
        assertEquals(2.4, s.get("std_dev"));
    }

    @Test
    void medianOfOddCountIsTheMiddleReading() {
        feed("t", 9, 1, 5);
        assertEquals(5L, statsFor("t").get("median"));
    }

    @Test
    void groupsByWhateverSensorNameAppears() {
        feed("co2_ppm", 400);
        feed("lux", 10, 20);
        assertEquals(2, aggregator.result().size());
        assertEquals(1, statsFor("co2_ppm").get("count"));
        assertEquals(2, statsFor("lux").get("count"));
    }

    @Test
    void singleReadingHasZeroStdDev() {
        feed("t", 21.5);
        assertEquals(0L, statsFor("t").get("std_dev"));
    }

    @Test
    void ignoresOtherRecordTypes() {
        aggregator.accept(new DeviceEvent("d1", Severity.INFO));
        assertEquals(Map.of(), aggregator.result());
    }

    @Test
    void emptyResultWhenNothingAccepted() {
        assertEquals(Map.of(), aggregator.result());
        assertEquals("sensors", aggregator.name());
    }
}
