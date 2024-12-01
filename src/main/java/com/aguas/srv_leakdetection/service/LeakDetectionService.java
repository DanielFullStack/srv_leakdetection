package com.aguas.srv_leakdetection.service;

import com.aguas.srv_leakdetection.model.PressureReading;
import com.aguas.srv_leakdetection.repository.PressureReadingRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LeakDetectionService {

    private static final Logger log = LoggerFactory.getLogger(LeakDetectionService.class);
    private static final String REDIS_KEY_PREFIX = "pressure:";
    private static final double THRESHOLD = 5.0; // Variação máxima permitida em mca
    private static final String ALERT_TOPIC = "leakage-alerts";

    @Autowired
    private RedisService redisService;

    @Autowired
    private KafkaTemplate<String, PressureReading> kafkaTemplate;

    @Autowired
    private PressureReadingRepository repository;

    public void processPressureReading(PressureReading reading) {
        log.info("Processing pressure reading for sensor {}: {} mca", reading.getSensorId(), reading.getPressure());

        String redisKey = REDIS_KEY_PREFIX + reading.getSensorId();
        Double lastPressure = (Double) redisService.get(redisKey);

        log.debug("Last pressure reading from Redis for sensor {}: {}", reading.getSensorId(), lastPressure);

        if (lastPressure != null) {
            double variation = Math.abs(reading.getPressure() - lastPressure);
            log.debug("Calculated pressure variation for sensor {}: {} mca", reading.getSensorId(), variation);

            if (variation > THRESHOLD) {
                repository.save(reading);
                log.debug("Saved pressure reading for sensor {} to database", reading.getSensorId());

                log.warn("Leak detected for sensor {}: variation = {} mca", reading.getSensorId(), variation);
                kafkaTemplate.send(ALERT_TOPIC, reading.getSensorId(), reading);
                log.info("Alert sent to Kafka topic {} for sensor {}", ALERT_TOPIC, reading.getSensorId());
            }
        } else {
            log.info("No previous pressure reading found for sensor {}", reading.getSensorId());
        }

        // Atualiza leitura no Redis
        redisService.save(redisKey, reading, 1, TimeUnit.HOURS);
        log.debug("Updated pressure reading in Redis for sensor {}", reading.getSensorId());
    }
}