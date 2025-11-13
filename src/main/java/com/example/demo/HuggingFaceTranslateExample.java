package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class HuggingFaceTranslateExample {

    public static void main(String[] args) throws Exception {
        // Загрузка токена из config.properties
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        }
        String apiKey = properties.getProperty("huggingface.apiKey");
        String model = "Helsinki-NLP/opus-mt-en-ru";

        // Ввод текста с консоли
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите текст на английском: ");
        String text = scanner.nextLine();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("https://router.huggingface.co/hf-inference/models/" + model);
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity("{\"inputs\":\"" + text + "\"}"));

            String response = client.execute(request, httpResponse -> {
                int status = httpResponse.getCode();
                String body = new String(httpResponse.getEntity().getContent().readAllBytes());
                System.out.println("HTTP Status: " + status);
                return body;
            });

            // Парсинг JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);

            if (json.isArray() && json.get(0).has("translation_text")) {
                String translated = json.get(0).get("translation_text").asText();
                System.out.println("Перевод: " + translated);
            } else {
                System.out.println("Unexpected response format: " + json.toPrettyString());
            }
        }
    }
}
