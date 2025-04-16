package com.helloworld.kafka.springexamples.additional;

import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

import lombok.extern.slf4j.Slf4j;

/**
 * Ejemplo de consumidor con manejo de errores mejorado y DLQ (Dead Letter Queue)
 * Este ejemplo muestra cómo implementar un consumidor con mejor manejo de errores,
 * reintentos personalizados y envío a cola de mensajes muertos (DLQ).
 */
@SpringBootApplication
@Slf4j
public class EnhancedConsumer {

    public static void main(String[] args) {
        SpringApplication.run(EnhancedConsumer.class, args);
    }

    @Bean
    public NewTopic topic() {
        NewTopic topic = TopicBuilder.name("enhanced-topic")
                .partitions(3)
                .replicas(1)
                .build();
        log.info("Verificado topic {}", topic.name());
        return topic;
    }
    
    @Bean
    public NewTopic dlqTopic() {
        NewTopic topic = TopicBuilder.name("enhanced-topic-dlq")
                .partitions(1)
                .replicas(1)
                .build();
        log.info("Creado topic DLQ {}", topic.name());
        return topic;
    }
    
    @KafkaListener(id = "enhancedListener", topics = "enhanced-topic")
    public void listen(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_KEY) String key,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        @Headers Map<String, Object> headers,
        Acknowledgment acknowledgment) {
        
        try {
            log.info("Recibido mensaje: clave={}, valor={}, partición={}, offset={}", 
                    key, message, partition, offset);
            
            // Simulamos procesamiento del mensaje con validación
            if (!isValidMessage(message)) {
                throw new IllegalArgumentException("Mensaje inválido: " + message);
            }
            
            // Procesamiento normal
            processMessage(message);
            
            // Confirmamos manualmente el mensaje después de procesarlo correctamente
            acknowledgment.acknowledge();
            log.info("Mensaje procesado y confirmado correctamente");
            
        } catch (IllegalArgumentException e) {
            // Error de validación - enviar a DLQ y confirmar para no reprocesar
            log.warn("Error de validación, enviando a DLQ: {}", e.getMessage());
            sendToDLQ(key, message, headers, "VALIDATION_ERROR", e.getMessage());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            // Error de procesamiento - implementar lógica de reintentos
            log.error("Error al procesar el mensaje: {}", e.getMessage());
            
            // Obtener contador de reintentos de los headers o inicializar
            Integer retryCount = getRetryCount(headers);
            
            if (retryCount < 3) {
                // No confirmar para que se reprocese
                log.info("Reintento {}/3 para mensaje: clave={}, valor={}", 
                        retryCount + 1, key, message);
                // En una implementación real, podríamos usar un retraso exponencial
                // o enviar a un topic de reintentos con un retraso programado
            } else {
                // Máximo de reintentos alcanzado - enviar a DLQ y confirmar
                log.warn("Máximo de reintentos alcanzado, enviando a DLQ");
                sendToDLQ(key, message, headers, "MAX_RETRIES", e.getMessage());
                acknowledgment.acknowledge();
            }
        }
    }
    
    @KafkaListener(id = "dlqListener", topics = "enhanced-topic-dlq")
    public void listenDLQ(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_KEY) String key,
        @Headers Map<String, Object> headers) {
        
        log.info("DLQ - Recibido mensaje: clave={}, valor={}", key, message);
        log.info("DLQ - Error: {}", headers.get("error_reason"));
        log.info("DLQ - Descripción: {}", headers.get("error_description"));
        
        // Aquí se podría implementar lógica adicional para mensajes en DLQ
        // como notificaciones, alertas, o intentos de recuperación manual
    }
    
    private boolean isValidMessage(String message) {
        // Implementar lógica de validación
        return message != null && !message.isEmpty() && !message.contains("error");
    }
    
    private void processMessage(String message) {
        // Simulación de procesamiento del mensaje
        log.info("Procesando mensaje: {}", message);
        
        // Simulamos un procesamiento que podría fallar aleatoriamente
        if (Math.random() < 0.2) { // 20% de probabilidad de fallo
            throw new RuntimeException("Error simulado en el procesamiento del mensaje");
        }
        
        // Procesamiento exitoso
        log.info("Procesamiento completado para el mensaje");
    }
    
    private Integer getRetryCount(Map<String, Object> headers) {
        // Obtener contador de reintentos de los headers o inicializar
        Object retryHeader = headers.get("retry_count");
        Integer retryCount = 0;
        
        if (retryHeader != null) {
            try {
                retryCount = Integer.parseInt(retryHeader.toString());
            } catch (NumberFormatException e) {
                log.warn("No se pudo parsear el contador de reintentos: {}", retryHeader);
            }
        }
        
        // Incrementar para el próximo reintento
        headers.put("retry_count", retryCount + 1);
        
        return retryCount;
    }
    
    private void sendToDLQ(String key, String message, Map<String, Object> headers, 
                          String errorReason, String errorDescription) {
        // En una implementación real, aquí enviaríamos el mensaje a la DLQ
        // usando un KafkaTemplate
        log.info("Enviando a DLQ: clave={}, valor={}, error={}, descripción={}", 
                key, message, errorReason, errorDescription);
        
        // Agregar información de error a los headers
        headers.put("error_reason", errorReason);
        headers.put("error_description", errorDescription);
        
        // Aquí iría el código para enviar a la DLQ usando KafkaTemplate
    }
}
