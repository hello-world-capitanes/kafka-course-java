package com.helloworld.kafka.springexamples.additional;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

/**
 * Ejemplo de productor con manejo de errores mejorado y monitorización
 * Este ejemplo muestra cómo implementar un productor con mejor manejo de errores,
 * reintentos personalizados y monitorización de métricas.
 */
@SpringBootApplication
@Slf4j
public class EnhancedProducer {

    public static void main(String[] args) {
        SpringApplication.run(EnhancedProducer.class, args);
    }

    @Bean
    public NewTopic topic() {
        NewTopic topic = TopicBuilder.name("enhanced-topic")
                .partitions(3)
                .replicas(1)
                .build();
        log.info("Creado topic {}", topic.name());
        return topic;
    }

    @Bean
    public ApplicationRunner runner(KafkaTemplate<String, String> template) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            log.info("Productor mejorado con manejo de errores y monitorización");
            log.info("Características:");
            log.info("- Manejo de errores mejorado con reintentos personalizados");
            log.info("- Monitorización de métricas de envío");
            log.info("- Registro detallado de eventos");
            
            while (true) {
                System.out.println("Ingrese clave:valor para enviar a Kafka (escriba 'exit' para salir):");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                
                String[] data = input.split(":");
                if (data.length != 2) {
                    System.out.println("Formato incorrecto. Use 'clave:valor'");
                    continue;
                }
                
                // Envío con manejo de errores mejorado
                long startTime = System.currentTimeMillis();
                CompletableFuture<SendResult<String, String>> future = template.send("enhanced-topic", data[0], data[1]);
                
                future.whenComplete((result, ex) -> {
                    long latency = System.currentTimeMillis() - startTime;
                    if (ex == null) {
                        log.info("Mensaje enviado con éxito: clave={}, valor={}, offset={}, latencia={}ms",
                                data[0], data[1], result.getRecordMetadata().offset(), latency);
                        
                        // Métricas de envío exitoso
                        logMetrics("success", latency, result.getRecordMetadata().partition());
                    } else {
                        log.error("Error al enviar mensaje: clave={}, valor={}, error={}, latencia={}ms",
                                data[0], data[1], ex.getMessage(), latency);
                        
                        // Métricas de envío fallido
                        logMetrics("failure", latency, -1);
                        
                        // Implementación de reintentos personalizados
                        retryMessage(template, data[0], data[1], 3);
                    }
                });
            }
            scanner.close();
        };
    }
    
    private void logMetrics(String status, long latency, int partition) {
        // En una implementación real, estas métricas se enviarían a un sistema de monitorización
        log.info("MÉTRICA - Status: {}, Latencia: {}ms, Partición: {}", 
                status, latency, partition >= 0 ? partition : "N/A");
    }
    
    private void retryMessage(KafkaTemplate<String, String> template, String key, String value, int maxRetries) {
        // Implementación de reintentos personalizados con backoff exponencial
        new Thread(() -> {
            int retries = 0;
            boolean sent = false;
            
            while (!sent && retries < maxRetries) {
                try {
                    // Backoff exponencial
                    long backoffTime = (long) Math.pow(2, retries) * 100;
                    Thread.sleep(backoffTime);
                    
                    retries++;
                    log.info("Reintentando envío (intento {}/{}): clave={}, valor={}", 
                            retries, maxRetries, key, value);
                    
                    CompletableFuture<SendResult<String, String>> future = template.send("enhanced-topic", key, value);
                    future.get(); // Esperar a que se complete el envío
                    
                    log.info("Reintento exitoso en el intento {}: clave={}, valor={}", retries, key, value);
                    sent = true;
                    
                } catch (Exception e) {
                    log.error("Error en reintento {}: clave={}, valor={}, error={}", 
                            retries, key, value, e.getMessage());
                    
                    if (retries >= maxRetries) {
                        log.error("Se agotaron los reintentos para: clave={}, valor={}", key, value);
                        // En un caso real, aquí se podría enviar a una cola de mensajes muertos (DLQ)
                    }
                }
            }
        }).start();
    }
}
