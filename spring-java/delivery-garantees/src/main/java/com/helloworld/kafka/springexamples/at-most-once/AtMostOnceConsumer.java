package com.helloworld.kafka.springexamples.atmostonce;

import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class AtMostOnceConsumer {

    public static void main(String[] args) {
        SpringApplication.run(AtMostOnceConsumer.class, args);
    }

    @Bean
    public NewTopic topic() {
        NewTopic topic = TopicBuilder.name("at-most-once-topic")
                .partitions(2)
                .replicas(1)
                .build();
        log.info("Verificado topic {}", topic.name());
        return topic;
    }
    
    @KafkaListener(id = "atMostOnceListener", topics = "at-most-once-topic")
    public void listen(
        @Payload String message,
        @Headers Map<String, Object> headers) {
        
        try {
            log.info("Recibido mensaje: {}", message);
            log.info("Headers: {}", headers);
            
            // Procesamos el mensaje inmediatamente
            // En AT-MOST-ONCE, el offset ya se ha confirmado automáticamente
            // antes de procesar el mensaje, por lo que si falla el procesamiento,
            // el mensaje no se volverá a procesar
            processMessage(message);
            
            log.info("Mensaje procesado correctamente");
            
        } catch (Exception e) {
            // Si ocurre un error durante el procesamiento, el mensaje ya está confirmado
            // y no se volverá a procesar (se pierde)
            log.error("Error al procesar el mensaje: {}. El mensaje se ha perdido.", e.getMessage());
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
