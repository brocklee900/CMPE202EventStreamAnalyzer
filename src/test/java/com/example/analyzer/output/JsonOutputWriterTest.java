package com.example.analyzer.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonOutputWriterTest {

    @TempDir Path tmp;

    private final JsonOutputWriter writer = new JsonOutputWriter();

    @Test
    void emptyMapIsWrittenAsEmptyObject() throws Exception {
        writer.write(tmp, "sensors", Map.of());
        assertEquals("{}", Files.readString(tmp.resolve("sensors.json")).strip());
    }

    @Test
    void usesTwoSpaceIndentation() throws Exception {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("count", 2);
        writer.write(tmp, "sensors", Map.of("humidity", inner));
        String expected = String.join("\n",
                "{",
                "  \"humidity\": {",
                "    \"count\": 2",
                "  }",
                "}");
        assertEquals(expected, Files.readString(tmp.resolve("sensors.json")).strip().replace("\r\n", "\n"));
    }

    @Test
    void wholeNumbersHaveNoDecimalPointAndDecimalsKeepOne() throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("minimum", 40L);
        data.put("average", 21.3);
        writer.write(tmp, "sensors", data);
        String json = Files.readString(tmp.resolve("sensors.json"));
        assertTrue(json.contains("\"minimum\": 40,"));
        assertFalse(json.contains("40.0"));
        assertTrue(json.contains("\"average\": 21.3"));
    }

    @Test
    void specialCharactersAreNotHtmlEscaped() throws Exception {
        writer.write(tmp, "access", Map.of("door=<A>", 1));
        assertTrue(Files.readString(tmp.resolve("access.json")).contains("\"door=<A>\""));
    }
}
