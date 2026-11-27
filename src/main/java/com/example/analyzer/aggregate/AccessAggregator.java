package com.example.analyzer.aggregate;

import com.example.analyzer.model.AccessRecord;
import com.example.analyzer.model.AccessResult;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class AccessAggregator extends RecordAggregator<AccessRecord> {

    static final int FLAG_THRESHOLD = 3;

    private final Map<String, DoorStats> doors = new TreeMap<>();

    public AccessAggregator() {
        super(AccessRecord.class, "access");
    }

    @Override
    protected void add(AccessRecord record) {
        doors.computeIfAbsent(record.door(), k -> new DoorStats()).add(record);
    }

    @Override
    protected Map<String, Object> summarize() {
        Map<String, Object> out = new LinkedHashMap<>();
        doors.forEach((door, stats) -> out.put(door, stats.summarize()));
        return out;
    }

    private static final class DoorStats {
        private final List<Long> durations = new ArrayList<>();
        private final Map<AccessResult, Integer> results = new EnumMap<>(AccessResult.class);
        private final Map<String, Integer> deniedByUser = new TreeMap<>();

        void add(AccessRecord record) {
            durations.add(record.durationMs());
            results.merge(record.result(), 1, Integer::sum);
            if (record.result() == AccessResult.DENIED) {
                deniedByUser.merge(record.user(), 1, Integer::sum);
            }
        }

        Map<String, Object> summarize() {
            List<Long> sorted = durations.stream().sorted().toList();

            Map<String, Object> decisionTimes = new LinkedHashMap<>();
            decisionTimes.put("min", sorted.get(0));
            decisionTimes.put("50_percentile", Stats.nearestRank(sorted, 50));
            decisionTimes.put("90_percentile", Stats.nearestRank(sorted, 90));
            decisionTimes.put("99_percentile", Stats.nearestRank(sorted, 99));
            decisionTimes.put("max", sorted.get(sorted.size() - 1));

            Map<String, Object> resultCounts = new LinkedHashMap<>();
            for (AccessResult result : AccessResult.values()) {
                resultCounts.put(result.name(), results.getOrDefault(result, 0));
            }

            List<String> flagged = deniedByUser.entrySet().stream()
                    .filter(e -> e.getValue() >= FLAG_THRESHOLD)
                    .map(Map.Entry::getKey)
                    .toList();

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("decision_times", decisionTimes);
            out.put("results", resultCounts);
            out.put("flagged_users", flagged);
            return out;
        }
    }
}
