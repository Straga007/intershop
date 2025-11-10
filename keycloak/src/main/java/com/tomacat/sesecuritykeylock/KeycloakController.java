package com.tomacat.sesecuritykeylock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class KeycloakController {

    private final KeycloakService keycloakService;

    public KeycloakController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @GetMapping("/token")
    public Mono<String> getToken() {
        return keycloakService.getClientSecret("myclient", "myclientsecret");
    }
}