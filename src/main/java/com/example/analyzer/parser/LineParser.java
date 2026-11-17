package com.example.analyzer.parser;

import java.util.Map;
import java.util.Optional;

/**
 * Turns one raw input line into a field map. Implement this for a new input
 * format (e.g. JSON Lines) without touching the rest of the pipeline.
 */
public interface LineParser {
    /** @return the fields, or empty if the line is unusable. */
    Optional<Map<String, String>> parse(String line);
}
