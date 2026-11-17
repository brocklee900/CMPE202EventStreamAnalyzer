package com.example.analyzer.cli;

/** Thrown for any bad command-line usage (missing/unknown args, file not found, ...). */
public class CliException extends Exception {
    public CliException(String message) {
        super(message);
    }
}
