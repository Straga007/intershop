package com.shop.spring.data.intershop.cash.redis;

import com.shop.spring.data.intershop.IntershopApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Objects;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = IntershopApplication.class)
@Import(RedisTestConfig.class)
@TestPropertySource(properties = {
    "spring.cache.type=redis"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class RedisIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedisOperations() {
        assertThat(redisTemplate).isNotNull();

        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();

        String key = "test:item:1";
        String value = "Test Item Value";
        redisTemplate.opsForValue().set(key, value);

        Object retrievedValue = redisTemplate.opsForValue().get(key);
        assertThat(retrievedValue).isEqualTo(value);

        Long dbSize = redisTemplate.getConnectionFactory().getConnection().dbSize();
        assertThat(dbSize).isPositive();
    }

    @Test
    void testRedisConnection() {
        Long dbSize = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().dbSize();
        assertThat(dbSize).isNotNull();
    }
}