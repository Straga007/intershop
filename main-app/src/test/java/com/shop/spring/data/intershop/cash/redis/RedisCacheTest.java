package com.shop.spring.data.intershop.cash.redis;

import com.shop.spring.data.intershop.IntershopApplication;
import com.shop.spring.data.intershop.service.ItemService;
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
class RedisCacheTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @Test
    void testGetItemWithCache() {
        assertThat(itemService).isNotNull();

        var item = itemService.getItemById("1").block();
        assertThat(item).isNotNull();
        assertThat(item.getId()).isEqualTo("1");

        Boolean hasKey = redisTemplate.hasKey("items::1");
        assertThat(hasKey).isTrue();
    }

    @Test
    void testClearCache() {
        var item = itemService.getItemById("1").block();
        assertThat(item).isNotNull();

        Boolean hasKey = redisTemplate.hasKey("items::1");
        assertThat(hasKey).isTrue();

        itemService.clearCache().block();

        hasKey = redisTemplate.hasKey("items::1");
        assertThat(hasKey).isFalse();
    }
}