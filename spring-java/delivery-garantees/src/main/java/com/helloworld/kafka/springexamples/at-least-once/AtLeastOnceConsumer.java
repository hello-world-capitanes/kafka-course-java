package com.helloworld.kafka.springexamples.atleastonce;

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

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class AtLeastOnceConsumer {

    public static void main(String[] args) {
        SpringApplication.run(AtLeastOnceConsumer.class, args);
    }

    @Bean
    public NewTopic topic() {
        NewTopic topic = TopicBuilder.name("at-least-once-topic")
                .partitions(2)
                .replicas(1)
                .build();
        log.info("Verificado topic {}", topic.name());
        return topic;
    }
    
    @KafkaListener(id = "atLeastOnceListener", topics = "at-least-once-topic")
    public void listen(
        @Payload String message,
        @Headers Map<String, Object> headers,
        Acknowledgment acknowledgment) {
        
        try {
            log.info("Recibido mensaje: {}", message);
            log.info("Headers: {}", headers);
            
            // Simulamos procesamiento del mensaje
            processMessage(message);
            
            // Confirmamos manualmente el mensaje después de procesarlo correctamente
            // Esto garantiza at-least-once: si falla antes de confirmar, se volverá a procesar
            acknowledgment.acknowledge();
            log.info("Mensaje procesado y confirmado correctamente");
            
        } catch (Exception e) {
            // Si ocurre un error durante el procesamiento, no confirmamos el mensaje
            // Esto hará que el mensaje se vuelva a procesar en el siguiente poll
            log.error("Error al procesar el mensaje: {}", e.getMessage());
            // No llamamos a acknowledgment.acknowledge() para que se reprocese
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
