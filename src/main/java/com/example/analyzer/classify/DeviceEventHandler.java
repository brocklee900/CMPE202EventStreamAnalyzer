package com.example.analyzer.classify;

import com.example.analyzer.model.DeviceEvent;
import com.example.analyzer.model.EventRecord;
import com.example.analyzer.model.Severity;

import java.util.Map;
import java.util.Optional;

public final class DeviceEventHandler extends RecordHandler {

    @Override
    protected Optional<EventRecord> tryCreate(Map<String, String> fields) {
        Optional<String> device = FieldValues.text(fields, "device");
        Optional<Severity> severity = FieldValues.enumValue(fields, "severity", Severity.class);
        if (device.isEmpty() || severity.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DeviceEvent(device.get(), severity.get()));
    }
}
