package com.taller3jee.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@Component
public class CursoManifestReader {

    private final ObjectMapper objectMapper;

    public CursoManifestReader() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> readManifest() {
        try {
            ClassPathResource resource = new ClassPathResource("batch-data/curso-manifest.json");
            try (InputStream is = resource.getInputStream()) {
                return objectMapper.readValue(is, Map.class);
            }
        } catch (Exception e) {
            log.error("Failed to read curso-manifest.json: {}", e.getMessage(), e);
            throw new RuntimeException("Cannot read curso manifest", e);
        }
    }
}
