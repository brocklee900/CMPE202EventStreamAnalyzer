package com.example.analyzer;

import com.example.analyzer.aggregate.AccessAggregator;
import com.example.analyzer.aggregate.Aggregator;
import com.example.analyzer.aggregate.DeviceAggregator;
import com.example.analyzer.aggregate.SensorAggregator;
import com.example.analyzer.classify.AccessRecordHandler;
import com.example.analyzer.classify.DeviceEventHandler;
import com.example.analyzer.classify.RecordHandler;
import com.example.analyzer.classify.SensorReadingHandler;
import com.example.analyzer.output.JsonOutputWriter;
import com.example.analyzer.output.OutputWriter;
import com.example.analyzer.parser.KeyValueLineParser;
import com.example.analyzer.parser.LineParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class AnalyzerPipeline {

    private final LineParser parser;
    private final RecordHandler classifier;
    private final List<Aggregator> aggregators;
    private final OutputWriter writer;

    public AnalyzerPipeline(LineParser parser, RecordHandler classifier,
                               List<Aggregator> aggregators, OutputWriter writer) {
        this.parser = parser;
        this.classifier = classifier;
        this.aggregators = aggregators;
        this.writer = writer;
    }

    public static AnalyzerPipeline createDefault() {
        return new AnalyzerPipeline(
                new KeyValueLineParser(),
                defaultClassifier(),
                defaultAggregators(),
                new JsonOutputWriter());
    }

    public static RecordHandler defaultClassifier() {
        return RecordHandler.chain(
                new SensorReadingHandler(),
                new DeviceEventHandler(),
                new AccessRecordHandler());
    }

    public static List<Aggregator> defaultAggregators() {
        return List.of(
                new SensorAggregator(),
                new DeviceAggregator(),
                new AccessAggregator());
    }

    public void run(Path inputFile, Path outDir) throws IOException {
        Files.createDirectories(outDir);

        try (Stream<String> lines = Files.lines(inputFile, StandardCharsets.UTF_8)) {
            lines.forEach(this::processLine);
        }

        for (Aggregator a : aggregators) {
            writer.write(outDir, a.name(), a.result());
        }
    }

    private void processLine(String line) {
        parser.parse(line)
                .flatMap(classifier::handle)
                .ifPresent(record -> aggregators.forEach(a -> a.accept(record)));
    }
}
