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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = IntershopApplication.class)
@Import(RedisTestConfig.class)
@TestPropertySource(properties = {
    "spring.cache.type=redis"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CacheAnnotationsTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testGetItemByIdCacheable() {
        assertThat(itemService).isNotNull();

        var result1 = itemService.getItemById("1").block();
        assertThat(result1).isNotNull();
        assertThat(result1.getId()).isEqualTo("1");

        Boolean hasKey = redisTemplate.hasKey("items::1");
        assertThat(hasKey).isTrue();

        var result2 = itemService.getItemById("1").block();
        assertThat(result2).isNotNull();
        assertThat(result2.getId()).isEqualTo("1");

        hasKey = redisTemplate.hasKey("items::1");
        assertThat(hasKey).isTrue();
    }

    @Test
    void testClearCacheEvict() {
        var result = itemService.getItemById("1").block();
        assertThat(result).isNotNull();

        Boolean hasKey = redisTemplate.hasKey("items::1");
        assertThat(hasKey).isTrue();

        itemService.clearCache().block();

        hasKey = redisTemplate.hasKey("items::1");
        assertThat(hasKey).isFalse();
    }
}