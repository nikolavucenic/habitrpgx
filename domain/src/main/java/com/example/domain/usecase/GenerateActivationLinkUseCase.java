package com.example.domain.usecase;

public class GenerateActivationLinkUseCase {

    public String execute(String baseUri, String token) {
        if (baseUri.contains("token=")) {
            return baseUri + token;
        }

        String normalizedBase = baseUri.endsWith("/")
                ? baseUri.substring(0, baseUri.length() - 1)
                : baseUri;
        return normalizedBase + "/activate?token=" + token;
    }
}
