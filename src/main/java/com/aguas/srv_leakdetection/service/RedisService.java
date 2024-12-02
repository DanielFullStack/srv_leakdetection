package com.aguas.srv_leakdetection.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void save(String key, Object value, long timeout, TimeUnit unit) {
        log.info("Saving key: {} with timeout: {} {}", key, timeout, unit);
        redisTemplate.opsForValue().set(key, value, timeout, unit);
        log.debug("Successfully saved key: {}", key);
    }

    public Object get(String key) {
        log.info("Getting value for key: {}", key);
        Object value = redisTemplate.opsForValue().get(key);
        log.debug("Retrieved value for key: {}", key);
        return value;
    }
}