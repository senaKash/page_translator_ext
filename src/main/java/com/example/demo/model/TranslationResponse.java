
//DTO для ответа апи HF
package com.example.demo.model;

public class TranslationResponse {
    private String translatedText;

    public TranslationResponse(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
