package com.example.analyzer.classify;

import com.example.analyzer.model.EventRecord;
import com.example.analyzer.model.SensorReading;

import java.util.Map;
import java.util.Optional;

public final class SensorReadingHandler extends RecordHandler {

    @Override
    protected Optional<EventRecord> tryCreate(Map<String, String> fields) {
        Optional<String> sensor = FieldValues.text(fields, "sensor");
        Optional<Double> reading = FieldValues.decimal(fields, "reading");
        if (sensor.isEmpty() || reading.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SensorReading(sensor.get(), reading.get()));
    }
}
