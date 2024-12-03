package com.aguas.srv_leakdetection.service;

import com.aguas.srv_leakdetection.model.LeakDetection;
import com.aguas.srv_leakdetection.repository.PressureReadingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeakDetectionServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisService redisService;

    @Mock
    private PressureReadingRepository repository;

    @InjectMocks
    private LeakDetectionService leakDetectionService;

    private LeakDetection reading;

    @BeforeEach
    void setUp() {
        reading = new LeakDetection();
        reading.setSensorId("sensor1");
        reading.setPressure(10.0);
    }

    @Test
    void shouldSavePressureToRedisWhenNoLastPressureExists() {
        when(redisService.get(anyString())).thenReturn(null);

        leakDetectionService.processPressureReading(reading);

        verify(redisService).save(eq("pressure:sensor1"), eq(10.0), eq(1L), eq(TimeUnit.HOURS));
        verify(repository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void shouldNotSendAlertWhenPressureVariationIsBelowThreshold() {
        when(redisService.get(anyString())).thenReturn(9.0);

        leakDetectionService.processPressureReading(reading);

        verify(redisService).save(eq("pressure:sensor1"), eq(10.0), eq(1L), eq(TimeUnit.HOURS));
        verify(repository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void shouldSendAlertWhenPressureVariationIsAboveThreshold() throws JsonProcessingException {
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn(
                "{\"id\":null,\"sensorId\":\"sensor1\",\"pressure\":10.0,\"variation\":7.0,\"readingDateTime\":null,\"version\":null}");

        when(redisService.get("pressure:sensor1")).thenReturn(3.0);

        leakDetectionService.processPressureReading(reading);

        verify(redisService).save(eq("pressure:sensor1"), eq(10.0), eq(1L), eq(TimeUnit.HOURS));
        verify(repository).save(any());
        verify(kafkaTemplate).send(eq("leakage-alerts"), eq("sensor1"), anyString());
    }

    @Test
    void shouldHandleJsonProcessingException() throws JsonProcessingException {
        // Configuração do Redis para simular uma leitura anterior
        when(redisService.get("pressure:sensor1")).thenReturn(3.0);

        // Configuração para lançar exceção ao serializar o objeto
        lenient().when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Error") {
                });

        // Execução
        leakDetectionService.processPressureReading(reading);

        // Verificações
        // Confirma que o Redis foi atualizado com a nova pressão
        verify(redisService).save(eq("pressure:sensor1"), eq(10.0), eq(1L), eq(TimeUnit.HOURS));

        // Confirma que o repositório salvou a leitura no banco
        verify(repository).save(any());
    }

    @Test
    void shouldRetryOnOptimisticLockingFailure() {
        when(repository.save(any()))
                .thenThrow(ObjectOptimisticLockingFailureException.class)
                .thenThrow(ObjectOptimisticLockingFailureException.class)
                .thenReturn(reading);

        leakDetectionService.updatePressureReading(reading);

        verify(repository, times(3)).save(any());
    }

    @Test
    void shouldThrowExceptionAfterMaxRetries() {
        when(repository.save(any()))
                .thenThrow(ObjectOptimisticLockingFailureException.class);

        try {
            leakDetectionService.updatePressureReading(reading);
        } catch (ObjectOptimisticLockingFailureException ex) {
            verify(repository, times(3)).save(any());
        }
    }
}
