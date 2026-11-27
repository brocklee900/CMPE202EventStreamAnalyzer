package com.example.analyzer.aggregate;

import com.example.analyzer.model.DeviceEvent;
import com.example.analyzer.model.Severity;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public final class DeviceAggregator extends RecordAggregator<DeviceEvent> {

    private final Map<Severity, Integer> totalsBySeverity = new EnumMap<>(Severity.class);
    private final Map<String, Map<Severity, Integer>> severitiesByDevice = new TreeMap<>();

    public DeviceAggregator() {
        super(DeviceEvent.class, "devices");
    }

    @Override
    protected void add(DeviceEvent record) {
        totalsBySeverity.merge(record.severity(), 1, Integer::sum);
        severitiesByDevice
                .computeIfAbsent(record.device(), k -> new EnumMap<>(Severity.class))
                .merge(record.severity(), 1, Integer::sum);
    }

    @Override
    protected Map<String, Object> summarize() {
        Map<String, Object> bySeverity = new LinkedHashMap<>();
        for (Severity severity : Severity.values()) {
            bySeverity.put(severity.name(), totalsBySeverity.getOrDefault(severity, 0));
        }

        Map<String, Object> byDevice = new LinkedHashMap<>();
        severitiesByDevice.forEach((device, counts) -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("total", counts.values().stream().mapToInt(Integer::intValue).sum());
            entry.put("most_frequent_severity", mostFrequent(counts).name());
            byDevice.put(device, entry);
        });

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("by_severity", bySeverity);
        out.put("by_device", byDevice);
        return out;
    }

    static Severity mostFrequent(Map<Severity, Integer> counts) {
        Severity best = null;
        int bestCount = 0;
        for (Severity severity : Severity.values()) {
            int count = counts.getOrDefault(severity, 0);
            if (count > bestCount) {
                best = severity;
                bestCount = count;
            }
        }
        if (best == null) {
            throw new IllegalArgumentException("no severities counted");
        }
        return best;
    }
}
