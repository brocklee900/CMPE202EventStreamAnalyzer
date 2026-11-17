package com.example.analyzer.output;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/** Writes an aggregated result to a file. Add a CsvOutputWriter for CSV support. */
public interface OutputWriter {
    void write(Path file, Map<String, Object> data) throws IOException;
}
