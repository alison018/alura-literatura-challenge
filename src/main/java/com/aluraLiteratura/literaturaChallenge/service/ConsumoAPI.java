package com.aluraLiteratura.literaturaChallenge.service;

import com.aluraLiteratura.literaturaChallenge.model.DatosLibro;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ConsumoAPI {
    private static final Logger logger = Logger.getLogger(ConsumoAPI.class.getName());
    private static final String API_BASE_URL = "https://gutendex.com/books?search=";
    private static final String LANGUAGE_URL = "https://gutendex.com/books?languages=";

    public List<DatosLibro> obtenerDatosLibro(String titulo) {
        String encodedTitulo = encodeValue(titulo);
        String url = API_BASE_URL + encodedTitulo;
        return fetchData(url);
    }

    public List<DatosLibro> obtenerLibrosPorIdioma(String idioma) {
        String url = LANGUAGE_URL + idioma;
        return fetchData(url);
    }

    private List<DatosLibro> fetchData(String url) {
        logger.info("Fetching data from URL: " + url);
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            logger.info("Response status code: " + statusCode);
            if (statusCode == 200) {
                logger.info("Response received: " + response.body());
                List<DatosLibro> libros = parseDatosLibro(response.body());
                logger.info("Number of books received: " + libros.size());
                return libros;
            } else {
                logger.severe("Failed to fetch data, status code: " + statusCode);
                return new ArrayList<>();
            }
        } catch (IOException | InterruptedException e) {
            logger.severe("Error fetching data: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private List<DatosLibro> parseDatosLibro(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            JsonNode results = root.path("results");

            if (results.isMissingNode() || !results.isArray()) {
                logger.severe("No se encontraron resultados en la respuesta de la API.");
                return new ArrayList<>();
            }

            List<DatosLibro> libros = new ArrayList<>();
            for (JsonNode result : results) {
                DatosLibro datosLibro = mapper.treeToValue(result, DatosLibro.class);
                libros.add(datosLibro);
            }
            return libros;
        } catch (IOException e) {
            logger.severe("Error parsing JSON response: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
