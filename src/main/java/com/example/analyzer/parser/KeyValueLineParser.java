package com.example.analyzer.parser;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class KeyValueLineParser implements LineParser {

    private static final char SEPARATOR = '=';
    private static final char QUOTE = '"';

    @Override
    public Optional<Map<String, String>> parse(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }

        Map<String, String> fields = new LinkedHashMap<>();
        int length = line.length();
        int pos = 0;

        while (pos < length) {
            pos = skipWhitespace(line, pos);
            if (pos >= length) {
                break;
            }

            int keyEnd = scanKey(line, pos);
            boolean hasSeparator = keyEnd < length && line.charAt(keyEnd) == SEPARATOR;
            if (!hasSeparator || keyEnd == pos) {
                pos = skipToken(line, pos);
                continue;
            }

            String key = line.substring(pos, keyEnd);
            int valueStart = keyEnd + 1;

            if (valueStart < length && line.charAt(valueStart) == QUOTE) {
                int closingQuote = line.indexOf(QUOTE, valueStart + 1);
                if (closingQuote < 0) {
                    break;
                }
                fields.put(key, line.substring(valueStart + 1, closingQuote));
                pos = closingQuote + 1;
            } else {
                int valueEnd = skipToken(line, valueStart);
                fields.put(key, line.substring(valueStart, valueEnd));
                pos = valueEnd;
            }
        }

        return fields.isEmpty() ? Optional.empty() : Optional.of(Collections.unmodifiableMap(fields));
    }

    private static int skipWhitespace(String line, int pos) {
        while (pos < line.length() && Character.isWhitespace(line.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    private static int skipToken(String line, int pos) {
        while (pos < line.length() && !Character.isWhitespace(line.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    private static int scanKey(String line, int pos) {
        while (pos < line.length()
                && line.charAt(pos) != SEPARATOR
                && !Character.isWhitespace(line.charAt(pos))) {
            pos++;
        }
        return pos;
    }
}
