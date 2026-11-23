package com.example.analyzer.parser;

import java.util.Map;
import java.util.Optional;

public interface LineParser {
    Optional<Map<String, String>> parse(String line);
}
