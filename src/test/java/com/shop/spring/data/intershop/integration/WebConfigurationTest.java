package com.shop.spring.data.intershop.integration;

import com.shop.spring.data.intershop.IntershopApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = IntershopApplication.class)
@AutoConfigureWebTestClient
@TestPropertySource(locations = "classpath:application-test.properties")
public class WebConfigurationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testStaticResourceAccessIsNotTrue() throws Exception {
        webTestClient.get()
                .uri("/static/css/main.css")
                .exchange()
                .expectStatus().isNotFound();
    }

}