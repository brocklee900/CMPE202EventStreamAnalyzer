package com.example.analyzer.aggregate;

import com.example.analyzer.model.SensorReading;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class SensorAggregator extends RecordAggregator<SensorReading> {

    private final Map<String, List<Double>> readingsBySensor = new TreeMap<>();

    public SensorAggregator() {
        super(SensorReading.class, "sensors");
    }

    @Override
    protected void add(SensorReading record) {
        readingsBySensor.computeIfAbsent(record.sensor(), k -> new ArrayList<>()).add(record.reading());
    }

    @Override
    protected Map<String, Object> summarize() {
        Map<String, Object> out = new LinkedHashMap<>();
        readingsBySensor.forEach((sensor, readings) -> out.put(sensor, summarize(readings)));
        return out;
    }

    private static Map<String, Object> summarize(List<Double> readings) {
        List<Double> sorted = readings.stream().sorted().toList();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("count", sorted.size());
        stats.put("minimum", Stats.round1(sorted.get(0)));
        stats.put("median", Stats.round1(Stats.median(sorted)));
        stats.put("average", Stats.round1(Stats.average(sorted)));
        stats.put("maximum", Stats.round1(sorted.get(sorted.size() - 1)));
        stats.put("std_dev", Stats.round1(Stats.populationStdDev(sorted)));
        return stats;
    }
}
