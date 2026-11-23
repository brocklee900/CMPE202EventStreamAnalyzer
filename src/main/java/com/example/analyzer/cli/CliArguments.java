package com.example.analyzer.cli;

import java.nio.file.Path;

public record CliArguments(Path file, Path outDir) {}
