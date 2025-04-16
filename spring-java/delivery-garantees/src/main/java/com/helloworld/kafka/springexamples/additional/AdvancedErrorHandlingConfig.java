package com.helloworld.kafka.springexamples.additional;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.util.backoff.FixedBackOff;

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
        // Se intenta 3 veces con 1 segundo entre intentos, luego se envía a DLQ
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> {
                    // Determinar el topic de DLQ basado en el topic original
                    String dlqTopic = record.topic() + "-dlq";
                    return new TopicPartition(dlqTopic, record.partition());
                });

        // Configurar reintentos con backoff fijo: intervalo de 1000ms y máximo 3 intentos
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));

        // En versiones modernas se utiliza setCommonErrorHandler en lugar de setErrorHandler
        factory.setCommonErrorHandler(errorHandler);

        // Configurar concurrencia para procesamiento paralelo
        factory.setConcurrency(3);

        return factory;
    }
}
