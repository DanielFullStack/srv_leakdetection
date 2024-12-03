package com.aguas.srv_leakdetection.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testSave() {
        String key = "testKey";
        String value = "testValue";
        long timeout = 60;
        TimeUnit unit = TimeUnit.SECONDS;

        redisService.save(key, value, timeout, unit);

        verify(valueOperations).set(key, value, timeout, unit);
    }

    @Test
    void testGet() {
        String key = "testKey";
        String expectedValue = "testValue";
        when(valueOperations.get(key)).thenReturn(expectedValue);

        Object result = redisService.get(key);

        assertEquals(expectedValue, result);
        verify(valueOperations).get(key);
    }
}
