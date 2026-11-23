package com.example.analyzer;

import com.example.analyzer.cli.CliArguments;
import com.example.analyzer.cli.CliException;
import com.example.analyzer.cli.CliParser;

import java.io.IOException;
import java.io.PrintStream;

public final class Main {

    private Main() {}

    public static void main(String[] args) {
        System.exit(run(args, System.err));
    }

    static int run(String[] args, PrintStream err) {
        try {
            CliArguments cli = CliParser.parse(args);
            EventStreamAnalyzer.createDefault().run(cli.file(), cli.outDir());
            return 0;
        } catch (CliException e) {
            err.println("Error: " + e.getMessage());
            err.println(CliParser.USAGE);
            return 1;
        } catch (IOException e) {
            err.println("I/O error: " + e.getMessage());
            return 2;
        }
    }
}
