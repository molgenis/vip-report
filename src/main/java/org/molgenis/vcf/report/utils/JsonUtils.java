package org.molgenis.vcf.report.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.molgenis.vcf.report.repository.JsonException;
import org.molgenis.vcf.utils.model.ValueDescription;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonUtils {
    public static final String MISSING = ".";

    private JsonUtils(){}

    public static String toJson(Object input) {
        try {
            return new ObjectMapper().writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage());
        }
    }

    public static Map<String, ValueDescription> collectNodes(Path jsonPath) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, ValueDescription> result = new LinkedHashMap<>();
        try {
            JsonNode rootObj = mapper.readTree(Files.newBufferedReader(jsonPath));
            JsonNode nodesObj = rootObj.get("nodes");
            for (JsonNode node : nodesObj) {
                if ("LEAF".equals(node.path("type").asText())) {
                    String cls = node.path("class").asText();
                    String label = node.path("label").asText();
                    String description = node.path("description").asText();
                    result.put(cls, new ValueDescription(label, description));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return result;
    }

    public static String writeJsonListValue(String value, String separator){
        return !value.equals(MISSING) ? toJson(value.split(separator)) : "[]";
    }

}
