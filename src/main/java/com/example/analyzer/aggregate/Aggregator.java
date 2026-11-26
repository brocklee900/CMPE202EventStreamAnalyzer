package com.example.analyzer.aggregate;

import com.example.analyzer.model.EventRecord;

import java.util.Map;

public interface Aggregator {

    void accept(EventRecord record);

    Map<String, Object> result();

    String outputFileName();
}
