package com.shop.spring.data.intershop.cash.redis;

import com.shop.spring.data.intershop.IntershopApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = IntershopApplication.class)
@Import(RedisTestConfig.class)
@TestPropertySource(properties = {
    "spring.cache.type=redis"
})
class RedisCacheIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // Очищаем кэш перед каждым тестом
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testRedisConnection() {
        // Проверяем, что RedisTemplate доступен
        assertThat(redisTemplate).isNotNull();

        // Записываем значение в Redis
        redisTemplate.opsForValue().set("testKey", "testValue");

        // Проверяем, что значение записано
        Object value = redisTemplate.opsForValue().get("testKey");
        assertThat(value).isEqualTo("testValue");

        // Проверяем размер базы данных
        Long dbSize = redisTemplate.getConnectionFactory().getConnection().dbSize();
        assertThat(dbSize).isPositive();
    }
}