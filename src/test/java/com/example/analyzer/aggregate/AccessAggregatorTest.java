package com.example.analyzer.aggregate;

import com.example.analyzer.model.AccessRecord;
import com.example.analyzer.model.AccessResult;
import com.example.analyzer.model.SensorReading;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.example.analyzer.model.AccessResult.DENIED;
import static com.example.analyzer.model.AccessResult.GRANTED;
import static org.junit.jupiter.api.Assertions.*;

class AccessAggregatorTest {

    private final AccessAggregator aggregator = new AccessAggregator();

    private void feed(String user, String door, AccessResult result, long durationMs) {
        aggregator.accept(new AccessRecord(user, door, result, durationMs));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> door(String door) {
        return (Map<String, Object>) aggregator.result().get(door);
    }

    @Test
    void summarizesTheSampleInputForDoorA() {
        feed("user-42", "door-A", DENIED, 30);
        feed("user-42", "door-A", DENIED, 28);
        feed("user-42", "door-A", DENIED, 35);
        feed("user-08", "door-A", GRANTED, 18);

        assertEquals(Map.of(
                "decision_times", Map.of(
                        "min", 18L, "50_percentile", 28L, "90_percentile", 35L,
                        "99_percentile", 35L, "max", 35L),
                "results", Map.of("GRANTED", 1, "DENIED", 3),
                "flagged_users", List.of("user-42")), door("door-A"));
    }

    @Test
    void percentilesUseNearestRankOnTenValues() {
        long[] durations = {50, 10, 40, 20, 100, 30, 60, 90, 70, 80};
        for (long d : durations) {
            feed("u", "door-X", GRANTED, d);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> times = (Map<String, Object>) door("door-X").get("decision_times");
        assertEquals(10L, times.get("min"));
        assertEquals(50L, times.get("50_percentile"));
        assertEquals(90L, times.get("90_percentile"));
        assertEquals(100L, times.get("99_percentile"));
        assertEquals(100L, times.get("max"));
    }

    @Test
    void twoDenialsAreNotFlagged() {
        feed("u1", "d", DENIED, 1);
        feed("u1", "d", DENIED, 1);
        assertEquals(List.of(), door("d").get("flagged_users"));
    }

    @Test
    void grantedAttemptsDoNotCountTowardsFlagging() {
        feed("u1", "d", DENIED, 1);
        feed("u1", "d", DENIED, 1);
        feed("u1", "d", GRANTED, 1);
        assertEquals(List.of(), door("d").get("flagged_users"));
    }

    @Test
    void denialsAreCountedPerDoor() {
        feed("u1", "d1", DENIED, 1);
        feed("u1", "d1", DENIED, 1);
        feed("u1", "d2", DENIED, 1);
        assertEquals(List.of(), door("d1").get("flagged_users"));
        assertEquals(List.of(), door("d2").get("flagged_users"));
    }

    @Test
    void multipleFlaggedUsersAreSorted() {
        for (int i = 0; i < 3; i++) {
            feed("zoe", "d", DENIED, 1);
            feed("adam", "d", DENIED, 1);
        }
        assertEquals(List.of("adam", "zoe"), door("d").get("flagged_users"));
    }

    @Test
    void bothResultsAreReportedEvenWhenZero() {
        feed("u", "d", GRANTED, 5);
        assertEquals(Map.of("GRANTED", 1, "DENIED", 0), door("d").get("results"));
    }

    @Test
    void ignoresOtherRecordTypesAndIsEmptyByDefault() {
        aggregator.accept(new SensorReading("t", 1));
        assertEquals(Map.of(), aggregator.result());
        assertEquals("access", aggregator.name());
    }
}
