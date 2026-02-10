package com.planotech.plano.helper;

import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
public class JsonUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> toMap(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, Object>>() {
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON format", e);
        }
    }

    public String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("JSON conversion failed", e);
        }
    }
}
