package com.example.analyzer;

import com.example.analyzer.aggregate.Aggregator;
import com.example.analyzer.output.JsonOutputWriter;
import com.example.analyzer.output.OutputWriter;
import com.example.analyzer.parser.KeyValueLineParser;
import com.example.analyzer.parser.LineParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Pipeline: read line -> parse -> offer to each aggregator -> write results.
 * Each stage is an interface so formats / record types can be swapped or added.
 */
public final class EventStreamAnalyzer {

    private final LineParser parser;
    private final List<Aggregator> aggregators;
    private final OutputWriter writer;

    public EventStreamAnalyzer(LineParser parser, List<Aggregator> aggregators, OutputWriter writer) {
        this.parser = parser;
        this.aggregators = aggregators;
        this.writer = writer;
    }

    public static EventStreamAnalyzer createDefault() {
        return new EventStreamAnalyzer(
                new KeyValueLineParser(),
                List.of(),
                new JsonOutputWriter());
    }

    public void run(Path inputFile, Path outDir) throws IOException {
        Files.createDirectories(outDir);

        try (Stream<String> lines = Files.lines(inputFile, StandardCharsets.UTF_8)) {
            lines.forEach(this::processLine);
        }

        // Always write every output file, even if empty ({}).
        for (Aggregator a : aggregators) {
            writer.write(outDir.resolve(a.outputFileName()), a.result());
        }
    }

    private void processLine(String line) {
        Optional<Map<String, String>> fields = parser.parse(line);
        if (fields.isEmpty()) return;            // unparseable -> skip silently
        for (Aggregator a : aggregators) {
            if (a.accept(fields.get())) return;  // first aggregator that claims the line wins
        }
        // matched nothing -> skip silently
    }
}
