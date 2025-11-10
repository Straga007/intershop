package com.tomacat.sesecuritykeylock;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class KeycloakService {

    private final WebClient webClient;

    public KeycloakService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8080")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    public Mono<String> getClientSecret(String clientId, String clientSecret) {
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "client_credentials");
        formData.put("client_id", clientId);
        formData.put("client_secret", clientSecret);

        StringBuilder formDataString = new StringBuilder();
        formData.forEach((key, value) -> formDataString.append(key).append("=").append(value).append("&"));
        if (formDataString.length() > 0) {
            formDataString.deleteCharAt(formDataString.length() - 1); // Remove last
        }

        return webClient.post()
                .uri("/realms/myrealm/protocol/openid-connect/token")
                .bodyValue(formDataString.toString())
                .retrieve()
                .bodyToMono(String.class);
    }
}