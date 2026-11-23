package com.example.analyzer.aggregate;

import java.util.Map;

public interface Aggregator {

    boolean accept(Map<String, String> fields);

    Map<String, Object> result();

    String outputFileName();
}
