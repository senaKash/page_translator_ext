package com.example.demo;

import static spark.Spark.*;

import com.example.demo.model.TranslationRequest;
import com.example.demo.model.TranslationResponse;
import com.example.demo.service.TranslationService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {

    public static void main(String[] args) {
        TranslationService service;
        try {
            service = new TranslationService("config.properties", "Helsinki-NLP/opus-mt-en-ru");
            System.out.println("TranslationService создан успешно!");
        } catch (Exception e) {
            System.err.println("Ошибка при создании TranslationService:");
            e.printStackTrace();
            return; // Завершаем сервер, если сервис не создаётся
        }

        ObjectMapper mapper = new ObjectMapper();

        port(8080); // Порт сервера

        // Обработка preflight для CORS
        options("/*", (request, response) -> {
            String headers = request.headers("Access-Control-Request-Headers");
            if (headers != null) response.header("Access-Control-Allow-Headers", headers);

            String method = request.headers("Access-Control-Request-Method");
            if (method != null) response.header("Access-Control-Allow-Methods", method);

            return "OK";
        });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        // POST /translate
        post("/translate", (req, res) -> {
            res.type("application/json");

            try {
                // Логируем подключение и тело запроса
                System.out.println("К серверу подключился клиент!");
                System.out.println("Тело запроса: " + req.body());

                TranslationRequest requestObj = mapper.readValue(req.body(), TranslationRequest.class);

                // Логируем текст перед переводом
                System.out.println("Текст для перевода: " + requestObj.getText());

                String translated = service.translate(requestObj).getTranslatedText();

                System.out.println("Переведённый текст: " + translated);

                TranslationResponse responseObj = new TranslationResponse(translated);
                return mapper.writeValueAsString(responseObj);
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        });

        System.out.println("Server started on http://localhost:8080");

        // --- Тесты через консоль ---
        System.out.println("Введите текст на английском для теста перевода (или empty для пропуска):");
        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            while (true) {
                String input = scanner.nextLine();
                if (input.isEmpty()) break;
                try {
                    TranslationRequest reqTest = new TranslationRequest(input);
                    String result = service.translate(reqTest).getTranslatedText();
                    System.out.println("Результат перевода: " + result);
                } catch (Exception e) {
                    System.err.println("Ошибка перевода: " + e.getMessage());
                }
                System.out.println("Введите текст на английском для теста перевода (или empty для пропуска):");
            }
        }
    }
}
