package com.example.analyzer.model;

import java.util.Objects;

public record SensorReading(String sensor, double reading) implements EventRecord {
    public SensorReading {
        Objects.requireNonNull(sensor);
    }
}
