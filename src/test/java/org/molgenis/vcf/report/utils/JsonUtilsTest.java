package org.molgenis.vcf.report.utils;

import org.junit.jupiter.api.Test;
import org.molgenis.vcf.utils.model.ValueDescription;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void testToJson() {
        Map<String, String> input = Map.of("a", "b");
        String json = JsonUtils.toJson(input);
        assertEquals("{\"a\":\"b\"}", json);
    }

    @Test
    void testWriteJsonListValueNormal() {
        String input = "x,y,z";
        String json = JsonUtils.writeJsonListValue(input, ",");
        assertEquals("[\"x\",\"y\",\"z\"]", json);
    }

    @Test
    void testWriteJsonListValueMissing() {
        assertEquals("[]", JsonUtils.writeJsonListValue(JsonUtils.MISSING, ","));
    }

    @Test
    void testCollectNodesLoadsLeafDescriptions() throws Exception {
        String content = """
                {
                  "nodes": [
                    { "type": "LEAF", "class": "B", "label": "label1", "description": "desc1" },
                    { "type": "BOOL", "label": "label2", "description": "desc2" },
                    { "type": "LEAF", "class": "P", "label": "label3", "description": "desc3" }
                  ]
                }
                """;
        Path file = Files.createTempFile("nodes-", ".json");
        Files.writeString(file, content);

        Map<String, ValueDescription> result = JsonUtils.collectNodes(file);

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(new ValueDescription("label1", "desc1"), result.get("B")),
                () -> assertEquals(new ValueDescription("label3", "desc3"), result.get("P"))
        );

        Files.deleteIfExists(file);
    }

    @Test
    void testCollectNodesThrowsOnMissingFile() {
        Path path = Path.of("file.json");
        assertThrows(java.io.UncheckedIOException.class, () -> JsonUtils.collectNodes(path));
    }
}
