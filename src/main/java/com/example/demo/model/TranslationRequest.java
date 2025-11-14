package com.example.demo.model;

public class TranslationRequest {
    private String text;

    // Публичный конструктор без аргументов для Jackson
    public TranslationRequest() { }

    public TranslationRequest(String text) {
        this.text = text;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
