package com.aguas.srv_leakdetection.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.aguas.srv_leakdetection.model.PressureReading;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, PressureReading> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092"); // Servidor Kafka
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // Serialização da chave
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // Serialização do valor
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, PressureReading> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    @Bean
    public ConsumerFactory<String, PressureReading> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092"); // Servidor Kafka
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "leak-detection-group"); // Grupo do consumidor
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // Desserialização da chave
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class); // Desserialização do valor
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // Confiar em todos os pacotes para deserialização
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, PressureReading.class); // Tipo padrão para o valor
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Desabilita commit automático de offsets
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Busca mensagens desde o início caso não haja
                                                                        // offsets
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // Limita o número de registros por poll
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // Tempo máximo de espera para consumir mensagens
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000); // Intervalo para enviar heartbeats ao Kafka
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000); // Tempo antes de considerar o consumidor morto
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PressureReading> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PressureReading> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Configurando o ErrorHandler com backoff exponencial
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(5); // Tenta 5 vezes
        backOff.setInitialInterval(500L); // Intervalo inicial: 500ms
        backOff.setMultiplier(2.0); // Dobra o tempo a cada tentativa
        backOff.setMaxInterval(5000L); // Máximo de 5 segundos entre tentativas

        factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));
        return factory;
    }
}
