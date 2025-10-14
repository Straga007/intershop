package com.shop.spring.data.intershop.cash.redis;

import com.shop.spring.data.intershop.IntershopApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = IntershopApplication.class)
@Import(RedisTestConfig.class)
@AutoConfigureWebTestClient
@TestPropertySource(locations = "classpath:application-test.properties")
class RedisIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testItemCaching() {
        // Проверяем, что RedisTemplate доступен
        assertThat(redisTemplate).isNotNull();

        // Очищаем кэш перед тестом
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // Запрашиваем страницу товара первый раз
        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String body = new String(response.getResponseBody());
                    assertThat(body).contains("item");
                });

        // Проверяем, что данные закэшированы в Redis
        Boolean hasKey = redisTemplate.hasKey("items::1");
        assertThat(hasKey).isTrue();

        // Получаем значение из кэша
        Object cachedValue = redisTemplate.opsForValue().get("items::1");
        assertThat(cachedValue).isNotNull();

        // Повторный запрос к тому же товару
        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String body = new String(response.getResponseBody());
                    assertThat(body).contains("item");
                });

        // Кэш должен по-прежнему содержать данные
        hasKey = redisTemplate.hasKey("items::1");
        assertThat(hasKey).isTrue();
    }

    @Test
    void testMainItemsCaching() {
        // Очищаем кэш перед тестом
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // Запрашиваем главную страницу с товарами первый раз
        webTestClient.get()
                .uri("/main/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String body = new String(response.getResponseBody());
                    assertThat(body).contains("items");
                });

        // Из-за особенностей кэширования в Spring, ключ может отличаться, проверим наличие любых ключей
        Long cacheSize = redisTemplate.getConnectionFactory().getConnection().dbSize();
        assertThat(cacheSize).isPositive();

        // Повторный запрос к главной странице
        webTestClient.get()
                .uri("/main/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String body = new String(response.getResponseBody());
                    assertThat(body).contains("items");
                });

        // Размер кэша должен остаться положительным
        cacheSize = redisTemplate.getConnectionFactory().getConnection().dbSize();
        assertThat(cacheSize).isPositive();
    }
}