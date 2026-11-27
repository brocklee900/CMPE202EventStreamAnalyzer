package com.example.analyzer.aggregate;

import com.example.analyzer.model.EventRecord;

import java.util.Map;
import java.util.Objects;

public abstract class RecordAggregator<T extends EventRecord> implements Aggregator {

    private final Class<T> recordType;
    private final String name;
    private int accepted;

    protected RecordAggregator(Class<T> recordType, String name) {
        this.recordType = Objects.requireNonNull(recordType);
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public final void accept(EventRecord record) {
        if (recordType.isInstance(record)) {
            add(recordType.cast(record));
            accepted++;
        }
    }

    @Override
    public final Map<String, Object> result() {
        return accepted == 0 ? Map.of() : summarize();
    }

    @Override
    public final String name() {
        return name;
    }

    protected abstract void add(T record);

    protected abstract Map<String, Object> summarize();
}
