package com.aguas.srv_leakdetection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.aguas.srv_leakdetection.model.PressureReading;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, PressureReading> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, PressureReading> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
