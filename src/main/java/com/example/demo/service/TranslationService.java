package com.example.demo.service;

import com.example.demo.model.TranslationRequest;
import com.example.demo.model.TranslationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class TranslationService {

    private final String apiKey;
    private final String model;

    public TranslationService(String tokenFilePath, String model) throws Exception {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        }
        this.apiKey = properties.getProperty("huggingface.apiKey");
        this.model = model;
    }

    public TranslationResponse translate(TranslationRequest request) throws Exception {
        System.out.println(apiKey);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://router.huggingface.co/hf-inference/models/" + model);
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity("{\"inputs\":\"" + request.getText() + "\"}"));

            String response = client.execute(post, httpResponse ->
                    new String(httpResponse.getEntity().getContent().readAllBytes())
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);

            if (json.isArray() && json.get(0).has("translation_text")) {
                return new TranslationResponse(json.get(0).get("translation_text").asText());
            } else {
                throw new RuntimeException("Unexpected response format: " + json.toPrettyString());
            }
        }
    }
}
