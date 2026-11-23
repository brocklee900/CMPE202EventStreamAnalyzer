package com.example.analyzer.output;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public interface OutputWriter {
    void write(Path file, Map<String, Object> data) throws IOException;
}
