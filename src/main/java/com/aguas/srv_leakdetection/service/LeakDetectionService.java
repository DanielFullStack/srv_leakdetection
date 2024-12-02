package com.aguas.srv_leakdetection.service;

import com.aguas.srv_leakdetection.model.LeakDetection;
import com.aguas.srv_leakdetection.repository.PressureReadingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LeakDetectionService {

    private static final Logger log = LoggerFactory.getLogger(LeakDetectionService.class);
    private static final String REDIS_KEY_PREFIX = "pressure:";
    private static final double THRESHOLD = 5.0; // Variação máxima permitida em mca
    private static final String ALERT_TOPIC = "leakage-alerts";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PressureReadingRepository repository;

    public LeakDetectionService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void processPressureReading(LeakDetection reading) {
        log.info("Processing pressure reading for sensor {}: {} mca", reading.getSensorId(), reading.getPressure());

        String redisKey = REDIS_KEY_PREFIX + reading.getSensorId();
        Double lastPressure = (Double) redisService.get(redisKey);

        log.debug("Last pressure reading from Redis for sensor {}: {}", reading.getSensorId(), lastPressure);

        if (lastPressure != null) {
            double variation = Math.abs(reading.getPressure() - lastPressure);
            log.debug("Calculated pressure variation for sensor {}: {} mca", reading.getSensorId(), variation);

            if (variation > THRESHOLD) {
                reading.setVariation(variation);
                updatePressureReading(reading);
                log.debug("Saved pressure reading for sensor {} to database", reading.getSensorId());

                log.warn("Leak detected for sensor {}: variation = {} mca", reading.getSensorId(), variation);
                try {
                    String message = objectMapper.writeValueAsString(reading);
                    kafkaTemplate.send(ALERT_TOPIC, reading.getSensorId(), message);
                    log.info("Alert sent to Kafka topic {} for sensor {}", ALERT_TOPIC, reading.getSensorId());
                } catch (JsonProcessingException e) {
                    log.error("Erro ao serializar leitura: {}", e.getMessage());
                }
            }
        } else {
            log.info("No previous pressure reading found for sensor {}", reading.getSensorId());
        }

        // Atualiza leitura no Redis
        redisService.save(redisKey, reading.getPressure(), 1, TimeUnit.HOURS);
        log.debug("Updated pressure reading in Redis for sensor {}", reading.getSensorId());
    }

    @Transactional
    public void updatePressureReading(LeakDetection reading) {
        int retryCount = 3;
        while (retryCount > 0) {
            try {
                repository.save(reading);
                return;
            } catch (ObjectOptimisticLockingFailureException ex) {
                log.warn("Concurrent update detected, retrying... Remaining attempts: {}", retryCount - 1);
                retryCount--;
                if (retryCount == 0) {
                    throw ex; // Lança exceção após tentativas
                }
            }
        }
    }

}