package com.example.demo;

import com.example.demo.model.TranslationRequest;
import com.example.demo.service.TranslationService;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        try {
            TranslationService service = new TranslationService("config.properties", "Helsinki-NLP/opus-mt-en-ru");
            Scanner scanner = new Scanner(System.in);

            System.out.println("Введите текст на английском для перевода:");
            String input = scanner.nextLine();

            String translated = service.translate(new TranslationRequest(input)).getTranslatedText();
            System.out.println("Перевод: " + translated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
