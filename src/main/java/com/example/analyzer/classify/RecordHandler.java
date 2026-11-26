package com.example.analyzer.classify;

import com.example.analyzer.model.EventRecord;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class RecordHandler {

    private RecordHandler next;

    public static RecordHandler chain(RecordHandler first, RecordHandler... rest) {
        RecordHandler current = first;
        for (RecordHandler handler : rest) {
            current = current.setNext(handler);
        }
        return first;
    }

    public RecordHandler setNext(RecordHandler next) {
        this.next = Objects.requireNonNull(next);
        return next;
    }

    public final Optional<EventRecord> handle(Map<String, String> fields) {
        Optional<EventRecord> record = tryCreate(fields);
        if (record.isPresent()) {
            return record;
        }
        return next == null ? Optional.empty() : next.handle(fields);
    }

    protected abstract Optional<EventRecord> tryCreate(Map<String, String> fields);
}
