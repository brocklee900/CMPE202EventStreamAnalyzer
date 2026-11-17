package com.example.analyzer.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CliParserTest {

    @TempDir Path tmp;

    @Test
    void missingFileArgumentIsAnError() {
        assertThrows(CliException.class, () -> CliParser.parse(new String[]{}));
    }

    @Test
    void nonexistentFileIsAnError() {
        assertThrows(CliException.class,
                () -> CliParser.parse(new String[]{"--file", tmp.resolve("nope.txt").toString()}));
    }

    @Test
    void flagWithoutValueIsAnError() {
        assertThrows(CliException.class, () -> CliParser.parse(new String[]{"--file"}));
    }

    @Test
    void unknownArgumentIsAnError() throws IOException {
        Path f = Files.createFile(tmp.resolve("in.txt"));
        assertThrows(CliException.class,
                () -> CliParser.parse(new String[]{"--file", f.toString(), "--bogus"}));
    }

    @Test
    void outdirDefaultsToCurrentDirectory() throws Exception {
        Path f = Files.createFile(tmp.resolve("in.txt"));
        CliArguments a = CliParser.parse(new String[]{"--file", f.toString()});
        assertEquals(Path.of("."), a.outDir());
    }

    @Test
    void outdirIsHonored() throws Exception {
        Path f = Files.createFile(tmp.resolve("in.txt"));
        CliArguments a = CliParser.parse(new String[]{"--file", f.toString(), "--outdir", "results"});
        assertEquals(Path.of("results"), a.outDir());
    }
}
