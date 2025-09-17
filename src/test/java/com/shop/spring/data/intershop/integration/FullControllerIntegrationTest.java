package com.shop.spring.data.intershop.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource(locations = "classpath:application-test.properties")
public class FullControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testFullUserJourney() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main/items");

        webTestClient.get()
                .uri("/main/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assert body != null;
                    assert body.contains("Витрина товаров");
                });

        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assert body != null;
                    assert body.contains("Ноутбук Dell");
                });

        webTestClient.post()
                .uri("/items/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("action=PLUS")
                .exchange()
                .expectStatus().is3xxRedirection();

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assert body != null;
                    assert body.contains("Корзина");
                });

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection();

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assert body != null;
                    assert body.contains("Заказы");
                });
    }
}
