package com.aguas.srv_leakdetection.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class RedisConfigTest {
    
    @Test
    public void testRedisTemplateConfiguration() {
        RedisConfig redisConfig = new RedisConfig();
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        
        RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate(connectionFactory);
        
        assertNotNull(redisTemplate);
        assertEquals(connectionFactory, redisTemplate.getConnectionFactory());
    }
}
