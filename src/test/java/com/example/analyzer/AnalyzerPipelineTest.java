package com.example.analyzer;

import com.example.analyzer.aggregate.Aggregator;
import com.example.analyzer.model.AccessRecord;
import com.example.analyzer.model.DeviceEvent;
import com.example.analyzer.model.EventRecord;
import com.example.analyzer.model.SensorReading;
import com.example.analyzer.output.JsonOutputWriter;
import com.example.analyzer.parser.KeyValueLineParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnalyzerPipelineTest {

    @TempDir Path tmp;

    private static final String SAMPLE = String.join("\n",
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

    private static final class RecordingAggregator implements Aggregator {
        final List<EventRecord> seen = new ArrayList<>();

        @Override
        public void accept(EventRecord record) {
            seen.add(record);
        }

        @Override
        public Map<String, Object> result() {
            return Map.of("count", seen.size());
        }

        @Override
        public String name() {
            return "recorded";
        }
    }

    @Test
    void classifiedRecordsReachAggregatorsAndBadLinesAreSkipped() throws Exception {
        Path input = Files.writeString(tmp.resolve("in.txt"), String.join("\n",
                "ts=1 sensor=t reading=1.5",
                "ts=2 severity=INFO device=d1",
                "ts=3 ###CORRUPTED### !!!",
                "",
                "ts=4 sensor=t reading=abc",
                "ts=5 user=u door=d result=DENIED duration_ms=7"));

        RecordingAggregator aggregator = new RecordingAggregator();
        Map<String, Map<String, Object>> written = new HashMap<>();

        new AnalyzerPipeline(
                new KeyValueLineParser(),
                AnalyzerPipeline.defaultClassifier(),
                List.of(aggregator),
                (dir, name, data) -> written.put(name, data))
                .run(input, tmp.resolve("out"));

        assertEquals(3, aggregator.seen.size());
        assertInstanceOf(SensorReading.class, aggregator.seen.get(0));
        assertInstanceOf(DeviceEvent.class, aggregator.seen.get(1));
        assertInstanceOf(AccessRecord.class, aggregator.seen.get(2));
        assertEquals(Map.of("count", 3), written.get("recorded"));
    }

    @Test
    void sampleInputProducesExpectedJsonFiles() throws Exception {
        Path input = Files.writeString(tmp.resolve("events.txt"), SAMPLE);
        Path out = tmp.resolve("results");

        AnalyzerPipeline.createDefault().run(input, out);

        assertJson(out.resolve("sensors.json"), """
                {
                  "humidity_percent": {"count": 1, "minimum": 45, "median": 45, "average": 45, "maximum": 45, "std_dev": 0},
                  "temperature_celsius": {"count": 2, "minimum": 21.5, "median": 23.3, "average": 23.3, "maximum": 25, "std_dev": 1.8}
                }""");
        assertJson(out.resolve("devices.json"), """
                {
                  "by_severity": {"CRITICAL": 1, "ERROR": 1, "WARN": 1, "INFO": 1},
                  "by_device": {
                    "thermostat-01": {"total": 2, "most_frequent_severity": "WARN"},
                    "door-lock-07": {"total": 2, "most_frequent_severity": "CRITICAL"}
                  }
                }""");
        assertJson(out.resolve("access.json"), """
                {
                  "door-A": {
                    "decision_times": {"min": 18, "50_percentile": 28, "90_percentile": 35, "99_percentile": 35, "max": 35},
                    "results": {"GRANTED": 1, "DENIED": 3},
                    "flagged_users": ["user-42"]
                  },
                  "door-B": {
                    "decision_times": {"min": 12, "50_percentile": 12, "90_percentile": 12, "99_percentile": 12, "max": 12},
                    "results": {"GRANTED": 1, "DENIED": 0},
                    "flagged_users": []
                  }
                }""");
        assertFalse(Files.readString(out.resolve("sensors.json")).contains("45.0"));
    }

    @Test
    void emptyInputStillWritesAllThreeFilesAsEmptyObjects() throws Exception {
        Path input = Files.writeString(tmp.resolve("empty.txt"), "ts=1 ###nothing useful###\n");
        Path out = tmp.resolve("results");

        AnalyzerPipeline.createDefault().run(input, out);

        for (String name : List.of("sensors.json", "devices.json", "access.json")) {
            assertEquals("{}", Files.readString(out.resolve(name)).strip(), name);
        }
    }

    @Test
    void outputDirectoryIsCreatedIfMissing() throws Exception {
        Path input = Files.writeString(tmp.resolve("in.txt"), "ts=1 sensor=t reading=1\n");
        Path out = tmp.resolve("a").resolve("b");

        AnalyzerPipeline.createDefault().run(input, out);

        assertTrue(Files.isRegularFile(out.resolve("sensors.json")));
    }

    private static void assertJson(Path file, String expected) throws Exception {
        JsonElement actual = JsonParser.parseString(Files.readString(file));
        assertEquals(JsonParser.parseString(expected), actual, file.getFileName().toString());
    }
}
