package com.example.analyzer.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Pretty-printed JSON (Gson's default indent is 2 spaces). Empty map -> {}. */
public final class JsonOutputWriter implements OutputWriter {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void write(Path file, Map<String, Object> data) throws IOException {
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(data, w);
        }
    }
}
