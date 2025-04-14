package com.helloworld.kafka.springexamples.exactlyonce;

import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class ExactlyOnceConsumer {

    public static void main(String[] args) {
        SpringApplication.run(ExactlyOnceConsumer.class, args);
    }

    @Bean
    public NewTopic topic() {
        NewTopic topic = TopicBuilder.name("exactly-once-topic")
                .partitions(2)
                .replicas(1)
                .build();
        log.info("Verificado topic {}", topic.name());
        return topic;
    }
    
    @KafkaListener(id = "exactlyOnceListener", topics = "exactly-once-topic")
    @Transactional
    public void listen(
        @Payload String message,
        @Headers Map<String, Object> headers,
        Acknowledgment acknowledgment) {
        
        try {
            log.info("Recibido mensaje: {}", message);
            log.info("Headers: {}", headers);
            
            // Procesamos el mensaje dentro de una transacción
            processMessage(message);
            
            // Confirmamos manualmente el mensaje después de procesarlo correctamente
            // La confirmación se realiza dentro de la misma transacción
            acknowledgment.acknowledge();
            log.info("Mensaje procesado y confirmado correctamente dentro de la transacción");
            
        } catch (Exception e) {
            // Si ocurre un error durante el procesamiento, la transacción se revierte
            // y el mensaje no se confirma, lo que garantiza exactly-once
            log.error("Error al procesar el mensaje: {}. La transacción se revertirá.", e.getMessage());
            throw e; // Relanzamos la excepción para que Spring revierta la transacción
        }
    }
    
    private void processMessage(String message) {
        // Simulación de procesamiento del mensaje
        log.info("Procesando mensaje: {}", message);
        
        // Simulamos un procesamiento que podría fallar aleatoriamente (para demostración)
        if (Math.random() < 0.1) { // 10% de probabilidad de fallo
            throw new RuntimeException("Error simulado en el procesamiento del mensaje");
        }
        
        // Procesamiento exitoso
        log.info("Procesamiento completado para el mensaje");
    }
}
