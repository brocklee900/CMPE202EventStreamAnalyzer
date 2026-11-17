package com.example.analyzer.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Parses {@code key=value key2="value with spaces"} lines. */
public final class KeyValueLineParser implements LineParser {

    @Override
    public Optional<Map<String, String>> parse(String line) {
        Map<String, String> fields = new LinkedHashMap<>();
        int i = 0, n = line.length();

        while (i < n) {
            while (i < n && Character.isWhitespace(line.charAt(i))) i++;
            if (i >= n) break;

            int start = i;
            while (i < n && line.charAt(i) != '=' && !Character.isWhitespace(line.charAt(i))) i++;
            if (i >= n || line.charAt(i) != '=') continue;   // token without '=' -> ignore

            String key = line.substring(start, i);
            i++; // skip '='

            String value;
            if (i < n && line.charAt(i) == '"') {
                int end = line.indexOf('"', i + 1);
                if (end < 0) {                       // unterminated quote: take the rest
                    value = line.substring(i + 1);
                    i = n;
                } else {
                    value = line.substring(i + 1, end);
                    i = end + 1;
                }
            } else {
                int vs = i;
                while (i < n && !Character.isWhitespace(line.charAt(i))) i++;
                value = line.substring(vs, i);
            }
            fields.put(key, value);
        }
        return fields.isEmpty() ? Optional.empty() : Optional.of(fields);
    }
}
