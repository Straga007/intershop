package com.shop.spring.data.intershop.cash.redis;

import com.shop.spring.data.intershop.IntershopApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = IntershopApplication.class)
@Import(RedisTestConfig.class)
@TestPropertySource(properties = {
    "spring.cache.type=redis"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class RedisCacheIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @Test
    void testRedisConnection() {
        assertThat(redisTemplate).isNotNull();

        redisTemplate.opsForValue().set("testKey", "testValue");

        Object value = redisTemplate.opsForValue().get("testKey");
        assertThat(value).isEqualTo("testValue");

        Long dbSize = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().dbSize();
        assertThat(dbSize).isPositive();
    }
}