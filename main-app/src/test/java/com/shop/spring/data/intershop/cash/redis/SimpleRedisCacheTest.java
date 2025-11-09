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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = IntershopApplication.class)
@Import(RedisTestConfig.class)
@TestPropertySource(properties = {
    "spring.cache.type=redis"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class SimpleRedisCacheTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedisTemplateOperations() {
        assertThat(redisTemplate).isNotNull();

        String key = "test:simple:key";
        String value = "test value";

        redisTemplate.opsForValue().set(key, value);

        Object retrievedValue = redisTemplate.opsForValue().get(key);
        assertThat(retrievedValue).isEqualTo(value);

        redisTemplate.delete(key);

        Boolean hasKey = redisTemplate.hasKey(key);
        assertThat(hasKey).isFalse();
    }

    @Test
    void testRedisConnection() {
        Long dbSize = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().dbSize();
        assertThat(dbSize).isNotNull();
    }
}