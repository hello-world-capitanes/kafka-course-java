package com.helloworld.kafka.springexamples.additional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Configuración avanzada para manejo de errores y reintentos en consumidores Kafka
 * Este ejemplo muestra cómo configurar un manejador de errores con reintentos y DLQ automáticos
 */
@Configuration
public class AdvancedErrorHandlingConfig {

    /**
     * Configuración de fábrica de contenedores de listeners con manejo de errores avanzado
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate) {
        
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        
        // Configurar modo de confirmación manual
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Configurar manejador de errores con reintentos y DLQ
        // Intentará 3 veces con 1 segundo entre intentos, luego enviará a DLQ
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> {
                    // Determinar el topic de DLQ basado en el topic original
                    String dlqTopic = record.topic() + "-dlq";
                    return new org.apache.kafka.common.TopicPartition(dlqTopic, record.partition());
                });
        
        // Configurar reintentos con backoff fijo
        // Parámetros: intervalo en ms, máximo número de intentos
        SeekToCurrentErrorHandler errorHandler = new SeekToCurrentErrorHandler(
                recoverer, new FixedBackOff(1000L, 3));
        
        factory.setErrorHandler(errorHandler);
        
        // Configurar concurrencia para procesamiento paralelo
        factory.setConcurrency(3);
        
        return factory;
    }
}
