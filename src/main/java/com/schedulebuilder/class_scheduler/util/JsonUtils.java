package com.schedulebuilder.class_scheduler.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JSON operations.
 */
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Parses a JSON string into a JsonNode.
     *
     * @param json The JSON string to parse.
     * @return A JsonNode representing the parsed JSON.
     * @throws Exception If parsing fails.
     */
    public static JsonNode parseJson(String json) throws Exception {
        return OBJECT_MAPPER.readTree(json);
    }
}
