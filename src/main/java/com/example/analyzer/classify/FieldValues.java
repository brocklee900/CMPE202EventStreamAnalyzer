package com.example.analyzer.classify;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

final class FieldValues {

    private static final Pattern DECIMAL = Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?");
    private static final Pattern INTEGER = Pattern.compile("[+-]?\\d+");

    private FieldValues() {}

    static Optional<String> text(Map<String, String> fields, String key) {
        String value = fields.get(key);
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    static Optional<Double> decimal(Map<String, String> fields, String key) {
        return text(fields, key)
                .filter(v -> DECIMAL.matcher(v).matches())
                .map(Double::parseDouble)
                .filter(Double::isFinite);
    }

    static Optional<Long> nonNegativeInteger(Map<String, String> fields, String key) {
        Optional<String> value = text(fields, key).filter(v -> INTEGER.matcher(v).matches());
        if (value.isEmpty()) {
            return Optional.empty();
        }
        try {
            long parsed = Long.parseLong(value.get());
            return parsed < 0 ? Optional.empty() : Optional.of(parsed);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    static <E extends Enum<E>> Optional<E> enumValue(Map<String, String> fields, String key, Class<E> type) {
        return text(fields, key).flatMap(v -> Arrays.stream(type.getEnumConstants())
                .filter(constant -> constant.name().equals(v))
                .findFirst());
    }
}
