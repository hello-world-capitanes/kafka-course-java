package com.helloworld.kafka.springexamples.additional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * Ejemplo de pruebas de rendimiento y fiabilidad para diferentes garantías de entrega
 * Este ejemplo muestra cómo realizar pruebas comparativas entre las diferentes
 * garantías de entrega para evaluar su impacto en rendimiento y fiabilidad.
 */
@SpringBootApplication
@EnableKafka
@Slf4j
public class PerformanceTestingExample {

    private static final int MESSAGE_COUNT = 10000;
    private static final CountDownLatch latch = new CountDownLatch(MESSAGE_COUNT);

    public static void main(String[] args) throws Exception {
        var context = SpringApplication.run(PerformanceTestingExample.class, args);
        
        // Obtener el KafkaTemplate del contexto
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> template = context.getBean(KafkaTemplate.class);
        
        // Ejecutar pruebas para cada garantía de entrega
        runPerformanceTest(template, "at-most-once-test", "at-most-once");
        runPerformanceTest(template, "at-least-once-test", "at-least-once");
        runPerformanceTest(template, "exactly-once-test", "exactly-once");
        
        // Esperar a que se procesen todos los mensajes
        boolean completed = latch.await(5, TimeUnit.MINUTES);
        log.info("Prueba completada: {}", completed ? "exitosamente" : "con timeout");
        
        // Cerrar la aplicación
        SpringApplication.exit(context, () -> 0);
    }
    
    @Bean
    public NewTopic atMostOnceTopic() {
        return TopicBuilder.name("at-most-once-test")
                .partitions(6)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic atLeastOnceTopic() {
        return TopicBuilder.name("at-least-once-test")
                .partitions(6)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic exactlyOnceTopic() {
        return TopicBuilder.name("exactly-once-test")
                .partitions(6)
                .replicas(1)
                .build();
    }
    
    @KafkaListener(id = "performanceTestListener", topics = {
            "at-most-once-test", "at-least-once-test", "exactly-once-test"})
    public void listen(String message) {
        // Simplemente contar los mensajes recibidos
        latch.countDown();
        
        // Registrar progreso cada 1000 mensajes
        long count = latch.getCount();
        if (count % 1000 == 0) {
            log.info("Mensajes restantes: {}", count);
        }
    }
    
    private static void runPerformanceTest(KafkaTemplate<String, String> template, 
                                          String topic, String guaranteeType) {
        log.info("Iniciando prueba de rendimiento para garantía: {}", guaranteeType);
        
        long startTime = System.currentTimeMillis();
        
        // Enviar mensajes en lote
        for (int index = 0; index < MESSAGE_COUNT; index++) {
            int i = index;
            String message = String.format("test-message-%s-%d", guaranteeType, i);
            
            if ("exactly-once".equals(guaranteeType)) {
                // Usar transacciones para exactly-once
                template.executeInTransaction(operations -> {
                    return operations.send(topic, "key-" + i, message);
                });
            } else {
                // Envío normal para at-most-once y at-least-once
                template.send(topic, "key-" + i, message);
            }
            
            // Registrar progreso cada 1000 mensajes
            if (i > 0 && i % 1000 == 0) {
                log.info("Enviados {} mensajes a {} ({})", i, topic, guaranteeType);
            }
        }
        
        long endTime = System.currentTimeMillis();
        double throughput = MESSAGE_COUNT * 1000.0 / (endTime - startTime);
        
        log.info("Prueba completada para {}", guaranteeType);
        log.info("Tiempo total: {} ms", endTime - startTime);
        log.info("Rendimiento: {} mensajes/segundo", String.format("%.2f", throughput));
    }
}
