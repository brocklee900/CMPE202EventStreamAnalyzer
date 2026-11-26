package com.example.analyzer;

import com.example.analyzer.aggregate.Aggregator;
import com.example.analyzer.model.AccessRecord;
import com.example.analyzer.model.DeviceEvent;
import com.example.analyzer.model.EventRecord;
import com.example.analyzer.model.SensorReading;
import com.example.analyzer.parser.KeyValueLineParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventStreamAnalyzerTest {

    @TempDir Path tmp;

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
        public String outputFileName() {
            return "recorded.json";
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
        Map<Path, Map<String, Object>> written = new HashMap<>();

        new EventStreamAnalyzer(
                new KeyValueLineParser(),
                EventStreamAnalyzer.defaultClassifier(),
                List.of(aggregator),
                written::put)
                .run(input, tmp.resolve("out"));

        assertEquals(3, aggregator.seen.size());
        assertInstanceOf(SensorReading.class, aggregator.seen.get(0));
        assertInstanceOf(DeviceEvent.class, aggregator.seen.get(1));
        assertInstanceOf(AccessRecord.class, aggregator.seen.get(2));
        assertEquals(Map.of("count", 3), written.get(tmp.resolve("out").resolve("recorded.json")));
    }
}
