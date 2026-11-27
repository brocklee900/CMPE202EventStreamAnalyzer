package com.example.analyzer.cli;

import java.nio.file.Files;
import java.nio.file.Path;

public final class CliParser {

    public static final String USAGE =
            "Usage: java -jar EventStreamAnalyzer.jar --file <filename.txt> [--outdir <directory>]";

    private CliParser() {}

    public static CliArguments parse(String[] args) throws CliException {
        String file = null;
        String outDir = ".";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--file" -> file = valueAfter(args, i++, "--file");
                case "--outdir" -> outDir = valueAfter(args, i++, "--outdir");
                default -> throw new CliException("Unknown argument: " + args[i]);
            }
        }

        if (file == null) {
            throw new CliException("Missing required argument --file");
        }
        Path filePath = Path.of(file);
        if (!Files.isRegularFile(filePath)) {
            throw new CliException("Input file not found: " + file);
        }
        return new CliArguments(filePath, Path.of(outDir));
    }

    private static String valueAfter(String[] args, int i, String flag) throws CliException {
        if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
            throw new CliException("Missing value for " + flag);
        }
        return args[i + 1];
    }
}
