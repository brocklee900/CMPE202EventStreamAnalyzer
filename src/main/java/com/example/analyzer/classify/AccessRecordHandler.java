package com.example.analyzer.classify;

import com.example.analyzer.model.AccessRecord;
import com.example.analyzer.model.AccessResult;
import com.example.analyzer.model.EventRecord;

import java.util.Map;
import java.util.Optional;

public final class AccessRecordHandler extends RecordHandler {

    @Override
    protected Optional<EventRecord> tryCreate(Map<String, String> fields) {
        Optional<String> user = FieldValues.text(fields, "user");
        Optional<String> door = FieldValues.text(fields, "door");
        Optional<AccessResult> result = FieldValues.enumValue(fields, "result", AccessResult.class);
        Optional<Long> durationMs = FieldValues.nonNegativeInteger(fields, "duration_ms");
        if (user.isEmpty() || door.isEmpty() || result.isEmpty() || durationMs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new AccessRecord(user.get(), door.get(), result.get(), durationMs.get()));
    }
}
