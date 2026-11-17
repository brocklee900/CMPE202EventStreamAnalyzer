package com.example.analyzer.aggregate;

import java.util.Map;

/**
 * One record type's classification + aggregation. To add a new record type
 * (e.g. Network Logs) add a new implementation and register it in
 * EventStreamAnalyzer.createDefault().
 */
public interface Aggregator {

    /**
     * Checks whether the line belongs to this record type (required keys present,
     * values valid). If so, consumes it and returns true; otherwise returns false.
     */
    boolean accept(Map<String, String> fields);

    /** Aggregated result, ready to serialize. Empty map if nothing was accepted. */
    Map<String, Object> result();

    /** e.g. "sensors.json" */
    String outputFileName();
}
