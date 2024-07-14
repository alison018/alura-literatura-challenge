package com.aluraLiteratura.literaturaChallenge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;
@Service
public class ConvierteDatos {
    private static final Logger logger = Logger.getLogger(ConvierteDatos.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T obtenerDatos(String json, Class<T> clase) {
        if (json == null || json.isEmpty()) {
            logger.severe("No content to map: JSON response is empty");
            throw new RuntimeException("No content to map: JSON response is empty");
        }
        try {
            return objectMapper.readValue(json, clase);
        } catch (JsonProcessingException e) {
            logger.severe("Error processing JSON: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
