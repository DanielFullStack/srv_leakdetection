package com.aguas.srv_leakdetection.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import com.aguas.srv_leakdetection.model.PressureReading;

public class KafkaConfigTest {
    
    private KafkaConfig kafkaConfig = new KafkaConfig();

    @Test
    public void testProducerFactory() {
        ProducerFactory<String, String> factory = kafkaConfig.producerFactory();
        assertNotNull(factory);
    }

    @Test
    public void testKafkaTemplate() {
        KafkaTemplate<String, String> template = kafkaConfig.kafkaTemplate();
        assertNotNull(template);
    }

    @Test
    public void testConsumerFactory() {
        ConsumerFactory<String, PressureReading> factory = kafkaConfig.consumerFactory();
        assertNotNull(factory);
    }

    @Test
    public void testKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PressureReading> factory = 
            kafkaConfig.kafkaListenerContainerFactory();
        assertNotNull(factory);
        assertTrue(factory.getConsumerFactory() != null);
    }
}
