package com.helloworld.kafka.springexamples.atmostonce.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "at-most-once-group");
        
        // Configuración específica para AT-MOST-ONCE
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true); // Commit automático
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100"); // Commit frecuente
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // Comenzar desde el último offset
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // Procesar más registros por poll
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
