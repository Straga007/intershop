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
@TestPropertySource(locations = "classpath:application-test.properties",
    properties = {
        "spring.sql.init.mode=never",
        "spring.r2dbc.initialization-mode=never"
    })
public class WebConfigurationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testStaticResourceAccessIsAllowedViaStaticPath() throws Exception {
        webTestClient.get()
                .uri("/static/images/laptop_dell.jpg")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testImagesResourceAccessIsAllowed() throws Exception {
        webTestClient.get()
                .uri("/images/laptop_dell.jpg")
                .exchange()
                .expectStatus().isOk();
    }
}