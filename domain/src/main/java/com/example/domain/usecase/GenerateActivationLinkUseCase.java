package com.example.domain.usecase;

public class GenerateActivationLinkUseCase {

    public String execute(String baseUri, String token) {
        String normalizedBase = baseUri.endsWith("/")
                ? baseUri.substring(0, baseUri.length() - 1)
                : baseUri;
        return normalizedBase + "/activate?token=" + token;
    }
}
