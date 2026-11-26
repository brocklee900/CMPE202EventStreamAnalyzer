package com.example.analyzer.model;

import java.util.Objects;

public record AccessRecord(String user, String door, AccessResult result, long durationMs) implements EventRecord {
    public AccessRecord {
        Objects.requireNonNull(user);
        Objects.requireNonNull(door);
        Objects.requireNonNull(result);
    }
}
