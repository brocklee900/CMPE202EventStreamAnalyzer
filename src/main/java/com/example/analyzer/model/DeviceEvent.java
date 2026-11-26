package com.example.analyzer.model;

import java.util.Objects;

public record DeviceEvent(String device, Severity severity) implements EventRecord {
    public DeviceEvent {
        Objects.requireNonNull(device);
        Objects.requireNonNull(severity);
    }
}
