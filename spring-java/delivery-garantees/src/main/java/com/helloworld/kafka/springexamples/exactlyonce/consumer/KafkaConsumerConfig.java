package com.helloworld.kafka.springexamples.exactlyonce.consumer;

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
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "exactly-once-group");
        
        // Configuración específica para EXACTLY-ONCE
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Desactivar commit automático
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"); // Solo leer mensajes confirmados
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Comenzar desde el principio si no hay offset
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
