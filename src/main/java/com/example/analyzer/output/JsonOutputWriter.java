package com.example.analyzer.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class JsonOutputWriter implements OutputWriter {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Override
    public void write(Path outDir, String name, Map<String, Object> data) throws IOException {
        Path file = outDir.resolve(name + ".json");
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(data, w);
            w.write(System.lineSeparator());
        }
    }
}
