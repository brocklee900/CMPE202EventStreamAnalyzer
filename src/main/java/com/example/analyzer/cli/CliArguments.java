package com.example.analyzer.cli;

import java.nio.file.Path;

/** Validated command-line arguments. */
public record CliArguments(Path file, Path outDir) {}
