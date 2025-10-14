package com.shop.spring.data.intershop.cash.redis;

import com.shop.spring.data.intershop.IntershopApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = IntershopApplication.class)
@Import(RedisTestConfig.class)
@TestPropertySource(properties = {
    "spring.cache.type=redis"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CacheConfigurationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void testRedisCacheManagerIsConfigured() {
        assertThat(cacheManager).isInstanceOf(RedisCacheManager.class);
    }

    @Test
    void testCacheManagerBeanExists() {
        assertThat(cacheManager).isNotNull();
    }

    @Test
    void testRedisConnectionFactoryExists() {
        assertThat(redisConnectionFactory).isNotNull();
    }

    @Test
    void testRedisConnectionWorks() {
        var connection = redisConnectionFactory.getConnection();
        assertThat(connection).isNotNull();
        
        var pong = connection.ping();
        assertThat(pong).isNotNull();
        
        connection.close();
    }
}